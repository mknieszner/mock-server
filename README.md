---

# Mock Server na podstawie plików HAR

## Wprowadzenie

Ten projekt jest serwerem mockującym, który odpowiada na żądania HTTP na podstawie wcześniej zapisanych plików HAR (HTTP Archive). Pliki HAR zawierają szczegółowe informacje o żądaniach HTTP, odpowiedziach i ich nagłówkach. Serwer dynamicznie generuje odpowiedzi na podstawie tych danych.

## Wymagania

- **Java 17 lub nowsza**
- **Maven** (jeśli używasz Mavena do budowania projektu)
- Pliki HAR w odpowiednim katalogu (więcej informacji poniżej)

## Struktura projektu

- `src/main/java` - Kod źródłowy projektu
    - `MockServerController.java` - Główna klasa odpowiedzialna za działanie serwera
- `src/main/resources/hars` - Katalog, w którym umieszczamy pliki HAR
- `pom.xml` - Plik konfiguracyjny Maven, zawierający zależności
- `README.md` - Niniejszy plik z instrukcjami

## Instalacja

### 1. Skopiuj projekt na lokalny komputer

Pobierz kod projektu lub sklonuj repozytorium.

### 2. Zainstaluj zależności

Jeśli używasz **Mavena**, wykonaj poniższe polecenie w katalogu głównym projektu, aby pobrać zależności.

```bash
mvn clean install
```

## Konfiguracja

### 1. Pliki HAR

Pliki HAR muszą znajdować się w katalogu `src/main/resources/hars`. Każdy plik HAR musi mieć rozszerzenie `.har` i być poprawnie sformatowanym plikiem JSON zgodnym ze standardem HAR (HTTP Archive).

Przykładowy plik HAR znajduje się w `src/main/resources/sample.har`. Możesz dodać więcej plików HAR w katalogu `hars`.

**Struktura katalogu:**

```
src/main/resources/hars/
    ├── sample.har
    └── other-example.har
```

### 2. Zawartość pliku HAR

Każdy plik HAR powinien zawierać informacje o żądaniach (request) i odpowiedziach (response). Przykład pliku HAR wygląda następująco:

```json
{
  "log": {
    "version": "1.2",
    "creator": {
      "name": "Example HAR Generator",
      "version": "1.0"
    },
    "entries": [
      {
        "request": {
          "method": "GET",
          "url": "http://localhost:8087/api/users?id=123",
          "headers": [
            {
              "name": "Accept",
              "value": "application/json"
            }
          ],
          "queryString": [
            {
              "name": "id",
              "value": "123"
            }
          ]
        },
        "response": {
          "status": 200,
          "headers": [
            {
              "name": "Content-Type",
              "value": "application/json"
            }
          ],
          "content": {
            "text": "{\"id\": 123, \"name\": \"John Doe\"}"
          }
        }
      }
    ]
  }
}
```

## Uruchomienie serwera

### 1. Uruchomienie za pomocą Maven

Jeśli używasz **Mavena**, uruchom serwer lokalny za pomocą następującego polecenia:

```bash
./mvnw spring-boot:run
```

### 2. Uruchomienie za pomocą IDE

Możesz także uruchomić projekt w dowolnym IDE, takim jak IntelliJ IDEA lub Eclipse. Otwórz projekt i uruchom klasę `MockServerController` jako aplikację Spring Boot.

### 3. Testowanie

Po uruchomieniu serwera, domyślnie działa on na porcie `8087`. Możesz przetestować serwer, wysyłając żądania HTTP (np. za pomocą Postman, Curl lub przeglądarki).

Przykładowe zapytania:

1. **GET** `/api/users?id=123`
   ```bash
   curl -X GET "http://localhost:8087/api/users?id=123"
   ```

2. **POST** `/api/users`
   ```bash
   curl -X POST "http://localhost:8087/api/users" -H "Content-Type: application/json" -d '{"name":"Jane Doe"}'
   ```

## Struktura działania

1. **Ładowanie plików HAR**: Podczas uruchamiania serwer wczytuje wszystkie pliki HAR z katalogu `src/main/resources/hars`.

2. **Dopasowanie żądań HTTP**: Kiedy przychodzi żądanie HTTP do serwera, porównywane są następujące elementy:
    - **Metoda HTTP** (GET, POST, PUT, DELETE)
    - **Ścieżka URL** (np. `/api/users`)
    - **Parametry zapytania** (query parameters)

3. **Generowanie odpowiedzi**: Jeśli odpowiednie żądanie znajduje się w plikach HAR, serwer zwraca zdefiniowaną odpowiedź wraz z nagłówkami i treścią.

4. **Błąd 404**: Jeśli żądanie nie pasuje do żadnego wpisu w plikach HAR, serwer zwróci błąd 404.

## Rozwiązywanie problemów

1. **Brak plików HAR**: Jeśli serwer zwraca wyjątek dotyczący braku plików, upewnij się, że pliki HAR są poprawnie umieszczone w katalogu `src/main/resources/hars`.

2. **Błędy parsowania**: Upewnij się, że pliki HAR mają poprawną strukturę JSON. Każdy plik powinien być poprawnie sformatowanym plikiem HAR zgodnym ze specyfikacją HAR.

3. **Problemy z portem**: Jeśli port 8087 jest już zajęty, możesz zmienić domyślny port, edytując plik `application.properties` w katalogu `src/main/resources/`:
   ```properties
   server.port=9090
   ```

---

## Kontakt

Jeśli masz pytania lub problemy, skontaktuj się z autorem projektu.

---
