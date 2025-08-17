# Śledzenie procesu w architekturze rozproszonej

Aby określić, jak przebiega proces w architekturze rozproszonej, potrzebujemy mechanizmu do jego śledzenia (trackowania).

Przykład:
- Z zewnątrz przychodzi request do **gateway**, który przekierowuje go do `customer-service`.
- `customer-service` wywołuje `payment-service`, aby w trakcie tego procesu utworzyć portfel.
- Jeśli coś się wysypie w `payment-service`, musimy wiedzieć, że ten request pochodził od `customer-service`, a nie bezpośrednio z zewnątrz.

Dlatego konieczne jest, aby mieć możliwość **trackowania** całego procesu.

---

## Mechanizm trackowania

W tym celu często stosuje się podejście, gdzie dla każdego procesu generowany jest **unikalny identyfikator** i przekazywany w każdym request i response jako nagłówek HTTP.

- Identyfikator może być przyjęty z zewnątrz (jeśli jest podany).
- Jeśli nie został podany, jest automatycznie generowany w pierwszym miejscu przyjmującym request, czyli zazwyczaj w **gateway**.
- W przypadku procesów, które nie przechodzą przez gateway, identyfikator jest generowany wewnątrz aplikacji.

Dzięki temu możemy śledzić cały przepływ (flow) i łatwo znaleźć miejsce problemu.  
Na przykład, korzystając z ELK Stack, wystarczy wpisać dany identyfikator, aby zobaczyć pełną ścieżkę przetwarzania danego requestu.

---

## Implementacja

### Dodawanie nagłówka `X-Trace-Id` i logowanie po stronie gateway

Nagłówek `X-Trace-Id` jest dodawany, jeśli nie został podany w przychodzącym request. Dodatkowo trace-id jest logowany.

📄 Gateway: [TraceIdFilter.java](gateway/src/main/java/pl/kopytka/TraceIdFilter.java)

### Obsługa po stronie mikroserwisów

#### Dodawanie nagłówka

Jeśli nie został podany (nie zawsze wszystko przychodzi z gateway, np. jakiś wewnętrzny proces odpalany przez scheduler):

📄 [TraceIdFeignInterceptor.java](common/src/main/java/pl/kopytka/common/tracing/TraceIdFeignInterceptor.java)

#### Logowanie trace-id

📄 [LoggingFilter.java](common/src/main/java/pl/kopytka/common/tracing/LoggingFilter.java)
