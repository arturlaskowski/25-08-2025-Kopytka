# Modularizacja systemu: Order i Customer jako osobne moduły

W tym etapie **Order** i **Customer** stały się osobnymi modułami.  
Moduł **Customer** nie wie nic o module **Order**.  
Natomiast moduł **Order** komunikuje się z **Customer** wyłącznie za pomocą jednego punktu - fasady: [CustomerFacade](src/main/java/pl/kopytka/customer/CustomerFacade.java)

---

## Testy

- Testy architektoniczne weryfikują, czy te założenia nie są złamane [ArchitectureTest](src/test/java/pl/kopytka/architecture/ArchitectureTest.java)
- Testy na najwyższym poziomie zabezpieczają nas, aby zweryfikować, czy refaktoryzacja nie zepsuła obserwowalnego zachowania systemu [CreateOrderEndToEndTest](src/test/java/pl/kopytka/CreateOrderEndToEndTest.java)

---
