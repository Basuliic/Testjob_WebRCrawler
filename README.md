# Web element searcher demo

Demonstrates simple web crawler that tries to find best possible match for element on another web page.
To do this it uses provided Id of element and than search for it in first provided file. After collecting information programm 
will look onto second file and find all matches for element and then return element with most matches found.

## Setup

This project requires at least the most recent Java 1.8.

Most of the project can be built with Maven.

You can launch program by using web_crawler.jar in target directory. All reqired libraries are put to  dependency-jars directory.
To run app use following command:
```shell script
-jar target\web_crawler.jar ./sample.html ./another.html
```
For Id app will use **"make-everything-ok-button"**. It is possible to change standard Id by providing third argument line:
```shell script
-jar target\web_crawler.jar ./sample.html ./another.html wrapper
