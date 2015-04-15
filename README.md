# Maze runner client

This is my solution to the coding task for the Zalando Technology contest @ BME Job Fair 2015 Spring.

## Task description

You wake up in a dark place without remembering what happened to you. You start to search around for an exit, and quickly realize that you are in a maze.
The *you* in this task is the maze runner client, which tries to find the exit. The only way to know where it can go next is the maze runner service.
The service returns information about moves within the maze for adjacent squares. With this information the client is able to find its way out of the maze.

Write a program (in the language of your choice), that will interact as a REST API client with the provided server in order to find a path from the starting position of the maze to its exit.
Your program should take two command line parameters: `code` (maze code) and `url` (URL to maze service) and print the path.

### Notes

- It was required to create a "production ready" application where Clients make use of:
  - logging,
  - correct exception handling,
  - unit tests.

- My language choice was java.

## Running the client
After cloning and starting the server clone this project and start the application: `mvn clean test`.
