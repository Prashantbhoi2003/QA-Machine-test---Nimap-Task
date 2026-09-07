# Nimap FieldForceConnect - QA Automation Project

Maven + Selenium + TestNG (Page Object Model) framework, with a Cucumber/BDD version included, covering the core automation tasks from the machine test:

1. Automate Login Journey (parameterized) - `LoginTest.java` / `login.feature`
2. Verify Toast/Popup message after PunchIn - `PunchInTest.java` (Blocked by UI Issue - Reported as `BUG_01`)
3. Add Customer (parameterized) - `AddCustomerTest.java`


## Project Structure

nimap-qa-automation/
├── pom.xml
├── testng.xml
├── README.md
├── FieldForceConnect_QA_Test_Cases.xlsx
├── FieldForceConnect_PunchIn_Bug_Report.xlsx
├── src/test/java/com/nimap/
│   ├── base/BaseTest.java          # WebDriver setup/teardown
│   ├── pages/                      # Page Object Model classes (LoginPage, DashboardPage, AddCustomerPage)
│   ├── tests/                      # TestNG test classes (LoginTest, PunchInTest, AddCustomerTest)
│   ├── utils/                      # ConfigReader + ExcelUtils
│   └── cucumber/                   # BDD Runner & Step Definitions
├── src/test/resources/
│   ├── config.properties           # Base URL & UI Locators
│   ├── features/login.feature      # BDD Cucumber Feature File
│   └── testdata/TestData.xlsx      # Parameterized Excel Test Data


## ⚠️ Key Test Finding & Bug Summary

During execution against `https://test.fieldforceconnect.com/`:
- Login Journey** and Add Customer modules were verified and automated via parameterized DataProviders.
- PunchIn Verification was blocked because the PunchIn button was missing/invisible on the Dashboard page.
- This issue has been logged as `BUG_01` in `FieldForceConnect_PunchIn_Bug_Report.xlsx`.

### Bug Summary Table:
| Bug ID | Module / Feature    | Bug Summary           | Severity | Priority | Status |
| BUG_01 | Dashboard / PunchIn | PunchIn button is not | High     | High     | Open   |
                                 visible on Dashboard 
                                 page,blocking PunchIn 
                                 toast validation 

## Configuration & Execution Steps

### Option 1: Run via Eclipse IDE
1. Open Eclipse and right-click on `testng.xml` or individual Test class.
2. Go to Run As > Run Configurations
3. Select the Arguments tab.
4. Under VM Arguments, enter your execution parameters:
   
   -DvalidUsername=bhoiprashant179@gmail.com -DvalidPassword=Test@1234

5. Click Apply and Run.

### Option 2: Run via Command Line / Maven
Run the full TestNG test suite by passing parameters directly in terminal:

bash
# Run full TestNG suite with credentials
mvn clean test -DvalidUsername=bhoiprashant179@gmail.com -DvalidPassword=Test@1234

# Run Cucumber / BDD Test Runner
mvn clean test -Dtest=CucumberTestRunner -DvalidUsername=bhoiprashant179@gmail.com -DvalidPassword=Test@1234

## Deliverables Included
1. Automation Source Code: Complete Maven + TestNG + Page Object Model + Cucumber setup.
2. `FieldForceConnect_QA_Test_Cases.xlsx`: Manual test cases & field validations.
3. `FieldForceConnect_PunchIn_Bug_Report.xlsx`:Standard bug report for the missing PunchIn button.
4. Postman Collection & Environment: API endpoints for Login and Add Customer validation.
