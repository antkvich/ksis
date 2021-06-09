#include <iostream>
#include <csignal>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <linux/ip.h>
#include <linux/icmp.h>
#include <linux/udp.h>
#include <unistd.h>

#define SRC_PORT 56972
#define DEST_PORT 33434

#define MAX_TTL 30
#define N_PROB 3

#define BUFFER_SIZE 1500

using namespace std;

sockaddr_in sin_send;
sockaddr_in sin_receive;
sockaddr_in sin_bind;

int send_fd;
int receive_fd;

socklen_t len;
bool timeout;
char receive_buffer[BUFFER_SIZE];

char str_ip[128];

void AlarmHandler(int signo) {
  timeout = true;
}

void ConfigureAlarmHandler() {
  struct sigaction act{}, oact{};
  act.sa_handler = AlarmHandler;
  sigemptyset(&act.sa_mask);
  act.sa_flags = 0;
  if (sigaction(SIGALRM, &act, &oact)) {
    cerr << "Sig action error!" << endl;
    exit(-1);
  }
}

void CreateSendSocket() {
  send_fd = socket(AF_INET, SOCK_DGRAM, 0);
  if (send_fd < 0) {// джамбограмма
    cerr << "Send socket error!" << endl;
    exit(-1);
  }
}

void CreateReceiveSocket() {
  receive_fd = socket(AF_INET, SOCK_RAW, IPPROTO_ICMP);
  setuid(getuid());
  if (receive_fd < 0) {
    cerr << "Receive socket error!" << endl;
    exit(-1);
  }
}

void Bind() {
  sin_bind = sockaddr_in{};
  sin_bind.sin_family = AF_INET;
  sin_bind.sin_port = htons(SRC_PORT);
  if (bind(send_fd, (sockaddr *) &sin_bind, sizeof(sin_bind)) < 0) {
    cerr << "Bind error!" << endl;
    exit(-1);
  }
}

void CreateSendAddress(const char *host) {
  sin_send = sockaddr_in{};
  sin_send.sin_family = AF_INET;
  sin_send.sin_port = htons(DEST_PORT);
  if (inet_pton(AF_INET, host, &sin_send.sin_addr) <= 0) {
    cerr << "Inet pton error!" << endl;
    exit(-1);
  }
}

/*
 * 0 - конечный узел
 * 1 - промежуточный узел
 * 2 - получен чужой ответ
 * 3 - таймаут
 * 4 - остальные ошибки
 */
int Receive(int port_offset) {
  timeout = false;

  int n;
  iphdr *ip_header1, *ip_header2;
  icmphdr *icmp_header;
  int ip_header_len1, ip_header_len2;
  int icmp_len;
  bool temp1, temp2;
  udphdr *udp_header;
  int result_code;

  alarm(3);
  while (true) {
    // Проверка таймаута
    if (timeout) {
      cout << "*  ";
      result_code = 3;
      break;
    }
    // Чтение данных
    len = sizeof(sin_send);
    n = recvfrom(receive_fd, receive_buffer, sizeof(receive_buffer), 0, (sockaddr *) &sin_receive, &len);
    if (n < 0) {
      if (errno == EINTR) {
        continue;
      } else {
        cerr << "Receive from error!" << endl;
        exit(-1);
      }
    }
    // Получение IPv4 заголовка
    ip_header1 = (iphdr *) receive_buffer;
    ip_header_len1 = ip_header1->ihl << 2;
    // Получение ICMP заголовка
    icmp_header = (icmphdr *) &receive_buffer[ip_header_len1];
    icmp_len = n - ip_header_len1;
    // Проверка законченности ICMP заголовка
    if (icmp_len < 8) {
      continue;
    }
    // Промежуточный узел
    temp1 = (icmp_header->type == ICMP_TIME_EXCEEDED) && (icmp_header->code == ICMP_EXC_TTL);
    // Конечный узел
    temp2 = (icmp_header->type == ICMP_DEST_UNREACH) && (icmp_header->code == ICMP_PORT_UNREACH);
    // Обработка ответа
    if (temp1 || temp2) {
      // Проверка законченности IPv4 заголовка внутри ICMP пакета
      if (icmp_len < (8 + sizeof(iphdr))) {
        continue;
      }
      // Получение IPv4 заголовка внутри ICMP пакета
      ip_header2 = (iphdr *) &receive_buffer[ip_header_len1 + 8];
      ip_header_len2 = ip_header2->ihl << 2;
      // Проверка законченности данных UDP портов
      if (icmp_len < (8 + ip_header_len2 + 4)) {
        continue;
      }
      // Получение по крайней мере той части UDP, содержайщей порты источника и назначения
      udp_header = (udphdr *) &receive_buffer[ip_header_len1 + 8 + ip_header_len2];
      // Проверка на соответствие
      if ((udp_header->source == htons(SRC_PORT)) && (udp_header->dest == htons(DEST_PORT + port_offset))) {
        inet_ntop(AF_INET, &sin_receive.sin_addr, str_ip, len);
        printf("%s  ", str_ip);
        result_code = temp1 ? 1 : 0;
      } else {
        result_code = 2;
      }
    } else {
      // неожидаемый вид ответа
      cout << "* ";
      result_code = 4;
    }
    break;
  }
  alarm(0);
  fflush(stdout);
  return result_code;
}

void Traceroute(const char *host) {
  printf("traceroute to %s, %d hops max, %d byte packets\n", host, MAX_TTL, 0);
  // Основной цикл
  bool stop = false;
  bool received;
  int port_offset = -1;
  for (int ttl = 1; ttl <= MAX_TTL; ++ttl) {
    printf("%2d  ", ttl);
    // Обновление параметра TTL
    if (setsockopt(send_fd, IPPROTO_IP, IP_TTL, &ttl, sizeof(ttl))) {
      cerr << "Set socket option error!" << endl;
      exit(-1);
    }
    // Отправка N_PROB пакетов с заданным TTL
    for (int i = 0; i < N_PROB; ++i) {
      // Изменение адреса
      port_offset++;
      sin_send.sin_port = htons(DEST_PORT + port_offset);
      // Отправка пакета
      if (sendto(send_fd, nullptr, 0, 0, (sockaddr *) &sin_send, sizeof(sin_send)) < 0) {
        cerr << "Send to error!" << endl;
        exit(-1);
      }
      // Получение ответа
      received = false;
      while (!received) {
        switch (Receive(port_offset)) {
          case 0: {
            stop = true;
          }
          case 1: {
            received = true;
            break;
          }
          case 2: {
            continue;
          }
          default: {
            received = true;
          }
        }
      }
    }
    cout << endl;
    // Выход при достижении конечного узла
    if (stop) {
      return;
    }
  }
}

int main(int argc, char **argv) {
  // Конфигурация будильника
  ConfigureAlarmHandler();
  // Создание отправляющего сокета
  CreateSendSocket();
  // Создание принимающего сокета
  CreateReceiveSocket();
  // Создание адреса привязки и привязка его к отправляющему сокету
  Bind();
  // Создание адреса отправки
  CreateSendAddress(argv[1]);
  // Выполнение основной логики
  Traceroute(argv[1]);
  return 0;
}
