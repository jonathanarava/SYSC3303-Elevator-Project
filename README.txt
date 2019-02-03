SYSC 3303 - Project Group 5 - Iteration 1

Group members and responsibilities:


Jonathan Arava: JUnit Testing, Diagrams, Elevator Class

Viktor Dimitrov: JUnit Testing, Diagrams, Elevator Class 

Calvin Lam: Scheduler Class, Diagrams 

Shaviyo Marasinghe: Elevator Class, Floor Class 

Brian Philip: Elevator Class, Floor Class

The above shows what each member contributed to significantly, but every member contributed passively in the programming of all classes.

-------------------------------------------------------------------------------------------------------------------------------------------

Files and setup instructions:


Scheduler.java:
	- This is the main class that runs Threads to the other classes
	- The function of the Scheduler is the schedule elevators and to qeueu the requests of each elevator request
	- It sends packets to the elevator class with the contents of the status of the elevator, the direction the elevator should move,
	  how long the motor should run, and the doors to open once it reaches its destination.

Elevator.java : 
	- It receives instruction packets from the scheduler and sends the current floor of the elevator which it gets from the sensor to
	  the scheduler

Floor.java (Incomplete):
	- Not in this iteration, but we created a skeleton to help for further iterations
	- It makes an array of floors for each Elevator Class and sets the bounds for the elevator to go up or down

Setup Instructions:
	1.



--------------------------------------------------------------------------------------------------------------------------------------------

Test files and test instructions:


We have an AllTests.java JUnit Class that runs all the of the JUnit tests for the following classes: 


ElevatorTest.java:
	- We created an empty constructor in the Elevator Class to run tests in methods 
	- Some of the tests have been commented out as they test code which is required in further iterations

FloorTest.java:
	- Empty skeleton of for the Floor Class methods tests was created

SchedulerTest.java:
	- 

We completed the coding for ElevatorTest and FloorTest, but the SchedulerTest is not yet complete, hence the AllTests Class is not yet runnable


