# Summary
Tutorial on scala for comprehensions, futures, separation of business logic and process flow logic.

This tutorial also demonstrates how to deal with failures from with blocking functions and compares that approach with returning ```Either``` from those functions.

# Overview
This tutorial highlights the use of _for comprehensions_ and ```Futures``` when needing to do a sequence of potentially blocking steps to complete some task.

A _for comphrension_ is a way to chain sequential map functions and will short circuit the chain if one of the maps fails.  It is useful when you have a conceptual sequential flow of steps that can only continue when the previous step has completed successfully. 

In this tutorial we will be building shapes, specifically a 3-D rectangle and then a 3-D tube that fits in that rectangle.  The steps are:
1. build a square base
1. convert the square into a 3-D box
1. create a tube that exactly fits in the box

To show how to handle _blocking_ steps, each step returns a ```Future``` and each step can fail.  For example we are only going to build circular tubes.

There are 2 approaches to failure:
1. throw an exception
1. return a holder class such as ```Option```, ```Try``` or ```Either```

This tutorial demonstrates throwing an exception in the ```exceptionbased``` packages and using an ```Either``` in the ```eitherbased``` packages.

/
# Organization. 
In each version, there are 2 packages: model and service.

## Model
The ```model``` package represents the shapes:
1. Circle
1. Square
1. Rectangle
1. Box
1. Tube

The ```model``` package also has the things that could go wrong:
1. case classes/objects that derive from the trait ```ConstructionError```
1. categorization of errors: ```ConstructionErrorCategory```

## Service
The ```service``` package represents the business logic of our system.  It is comprised of classes that build the individual shapes and a construction service that executes the steps to build/convert shapes into the resulting ```Tube```
The builders all have a unique ``` buildXYZ``` method to build the corresponding shape (circle, square, etc) and throw ```ConstructionError``` when necessary.  

The builder classes have state such as what is an invalid parameter etc.  This state is _not_ data specific and so it belongs to the class rather than the ```buildXYX``` method.

The builders include:
1. CircleBuilder
1. SquareBuilder
1. RectangleBuilder
1. BoxBuilder
1. TubeBuilder

There are 2 variations of the construction services.  
One takes the all of the builder classes in the constructor and calls the corresponding ```buildXYZ``` method.
The other takes the specific ```buildXYZ``` functions and calls those.

# Timeouts
Since the ```Services``` all return ```Futures```, special attention needs to be paid to the issue of ```java.util.concurrent.TimeoutException``` (yes scala reuses that exception).

The _for comphrehension_ per se does not throw / catch ```TimeoutException```, the exception needs to be handled where the result of the future is used.  In _eitherbased_ version of the code, this is in the ```Spec``` tests.
# Demonstration
To see how the ```Construction``` classes work there are unit tests ```ConstructionServiceSpec``` and ```ConstructionFunctionServiceSpec```.  These tests also demonstrate how to handle and map failed futures.

The ```Construction``` classes are only concerned with the flow logic, that is executing the steps in order and skipping steps when failures occur.  There are additional tests for the builder classes that are designed to test the business logic separately from the process flow.
