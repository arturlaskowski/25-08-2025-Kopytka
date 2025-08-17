# Współdzielona część w architekturze rozproszonej

Często potrzebujemy modułu dostarczanego do wszystkich mikroserwisów, aby zapewnić jednolite podejście do wspólnych aspektów, takich jak konfiguracje czy narzędzia.  
Taki moduł jest zwykle udostępniany jako biblioteka, która jest wersjonowana i wydawana niezależnie.

Warto pamiętać, by przy publikacji nowych wersji współdzielonej biblioteki:
- Informować konsumentów o zmianach (np. przez release notes).
- Unikać breaking changes, które mogłyby zepsuć cały system, ponieważ mikroserwisy mogą aktualizować bibliotekę w różnym tempie.

Przykład takiej współdzielonej biblioteki to [common](common).
