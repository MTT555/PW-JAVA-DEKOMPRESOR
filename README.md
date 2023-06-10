# **Dekompresor plików skompresowanych algorytmem Huffmana**
### ***Adrian Chmiel, Mateusz Tyl***
\
Program w języku Java realizujący dekompresję plików otrzymanych z kompresora naszego autorstwa w języku C. Projekt ten ma następującą strukturę:
* **kompresor** -> pakiet zawierający pliki źródłowe składające się na bibliotekę *Dekompresor.jar*
* **Dekompresor-GUI** -> zawiera pliki odpowiedzialne za część graficzną tego dekompresora
* **latex** -> zawiera wszelkie pliki pomocnicze użyte przy tworzeniu dokumentacji w LaTeX
* **test** -> zawiera testy o różnej złożoności do dyspozycji programisty
* **Dekompresor.jar** -> biblioteka zawierająca całość dekompresora
* **Readme.md** -> właśnie go czytasz :)
* **Specyfikacja_funkcjonalna.pdf**
* **Specyfikacja_implementacyjna.pdf**

Do prawidłowego uruchomienia projektu zalecane jest użycie JDK w wersji 20 lub wyższej!

W celu wyświetlenia prawidłowego sposobu uruchamiania programu w trybie wsadowym (a więc bez oprawy graficznej) można skorzystać z następującego polecenia znajdując się w głównym katalogu projektu:

> `java -jar Dekompresor.jar` \
> `java -jar Dekompresor.jar -h`
