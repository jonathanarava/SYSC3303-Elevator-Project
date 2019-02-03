SYSC 3303 - Project Group 5 - Iteration 1

Group members and responsibilities:


Jonathan Arava: JUnit Testing, Diagrams, Elevator Class, ElevatorIntermidiate Class

Viktor Dimitrov: JUnit Testing, Diagrams, Elevator Class, ElevatorIntermidiate Class

Calvin Lam: Scheduler Class, Diagrams, ElevatorIntermidiate Class

Shaviyo Marasinghe: Elevator Class, Floor Class, ElevatorIntermidiate Class

Brian Philip: Elevator Class, Floor Class, ElevatorIntermidiate Class

The above shows what each member contributed to significantly, but every member contributed passively in the programming of all classes.

-------------------------------------------------------------------------------------------------------------------------------------------

Files and setup instructions:


ElevatorIntermidiate.java:
	- Has input perameters on the number of Elevators required for the system
	- Creates multiple different threads of the Elevator Class
	- Handles packet sending and receiving to the Scheduler
	- Information to be sent to the Scheduler is taken by the Elevator threads

Scheduler.java:
	- This is the main class that handles sequencing and where the Elevator should be
	- This will get/send UDP packets from ElevatorIntermediate Class and in next iterations from the FloorIntermediate Class
	- This class will not handle any of the threading for multiple floors and elevators

Elevator.java : 
	- This is the actual Elevator
	- Multiple different threads of the Elevator will be created in the ElevatorIntermediate Class
	- Has getters and setters that the ElevatorIntermediate class can utilize

Floor.java (Incomplete):
	- Not in this iteration, but we created a skeleton to help for further iterations
	- It makes an array of floors for each Elevator Class and sets the bounds for the elevator to go up or down

FloorIntermidiate.java(Incomplete):
	- Similar to ElevatorIntermediate, this will take charge of threading for multiple Floor Classes

Setup Instructions:
	1. Run Scheduler.java
	2. Run ElevatorIntermidiate.java with 1 as a paramater (in the future more elevators can be implemented by changing the parameter)

--------------------------------------------------------------------------------------------------------------------------------------------

Test files and test instructions:


We have an AllTests.java JUnit Class that runs all the of the JUnit tests for the following classes: 

ElevatorTest.java:
	- We created an empty constructor in the Elevator Class to run tests in methods 
	- Some of the tests have been commented out as they test code which is required in further iterations

FloorTest.java:
	- Empty skeleton of for the Floor Class methods tests was created

SchedulerTest.java:
	- Incomplete
	- Some of the tests for the other classes (ElevatorIntermediate) were done in this test

We completed the coding for ElevatorTest and FloorTest, but the SchedulerTest is not yet complete, hence the AllTests Class is not yet runnable
