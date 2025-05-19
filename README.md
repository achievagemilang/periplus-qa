# Periplus Automation Tests

This project contains Selenium + TestNG automated tests for the Periplus online bookstore website. The tests cover login, searching and adding a book to the shopping cart, and verifying cart contents.

---

## Prerequisites

- Java JDK 8 or higher installed
- Maven installed and configured (`mvn` command accessible)
- Chrome browser installed
- ChromeDriver binary matching your Chrome version:
  - Download from: https://sites.google.com/chromium.org/driver/
  - Ensure the `chromedriver` executable is in your system `PATH`, or specify its location via system properties when running tests
  For Mac, I use Homebrew by running this command:
  ```bash
  brew install chromedriver
  ```

---

## Setup

1. Clone this repository:

    ```bash
    git clone https://github.com/your-repo/periplus-qa.git
    cd periplus-qa
    ```

2. Modify `config.properties.example` into `config.properties` file in the root directory. Fill it appropriately with your credentials.

    ```config.properties
    BASE_URL=https://www.periplus.com/
    EMAIL=<Your Email>
    PASSWORD=<Your Password>
    ```

   Replace `<Your Email>` and `<Your Password>` with valid Periplus user credentials (make sure it's registered).

---

## Running Tests

Run the automation tests using Maven:

```bash
mvn clean test
```

This will execute the TestNG suite defined in `testng.xml`.
