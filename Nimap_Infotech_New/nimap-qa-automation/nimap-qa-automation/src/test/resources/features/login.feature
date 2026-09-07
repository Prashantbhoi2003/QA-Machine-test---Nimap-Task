Feature: Login Journey
  As a FieldForceConnect user
  I want to log in with my credentials
  So that I can access my dashboard

  Scenario Outline: Login with different sets of credentials
    Given the user is on the login page
    When the user enters username "<username>" and password "<password>"
    And clicks the login button
    Then the login result should be "<expectedResult>"

    Examples:
      | username                 | password        | expectedResult |
      | valid_user@example.com   | ValidPass@123    | Success        |
      | invalid_user@example.com | WrongPass@123    | Failure        |
      |                          | ValidPass@123    | Failure        |
      | valid_user@example.com   |                  | Failure        |
