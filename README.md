# Branch startowy dla architektury rozproszonej z komunikacją synchroniczną

Aktualnie mamy wiele problemów i wyzwań, które pojawiły się po przejściu na architekturę rozproszoną.  
Waszym zadaniem jest znaleźć i wypisać listę problemów (wyzwań), które wynikają z faktu, że system **kopytka** jest teraz rozproszony i będzie skalowany.

## Założenia
- Każdy mikroserwis posiada własną, fizyczną bazę danych.
- Można pominąć kwestie związane z uwierzytelnianiem i autoryzacją.

## Jak mikroserwisy się ze sobą komunikują?

1. **Tworzenie klienta**
    - `customer-service`: `POST /api/customers`
    - Wysyłany jest request do **payment-service**, aby stworzyć portfel:
        - `POST /api/wallets`

2. **Tworzenie zamówienia**
    - `order-service`: `POST /api/orders`
    - Wysyłany jest request do **customer-service**, aby sprawdzić, czy klient istnieje:
        - `GET /api/customers/{id}`

3. **Płatność za zamówienie**
    - `order-service`: `POST /api/orders/pay`
    - Wysyłany jest request do **payment-service**, aby wykonać płatność:
        - `POST /api/payments/process`
