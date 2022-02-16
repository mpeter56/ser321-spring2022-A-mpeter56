# Activity 1

## Task1

### Question 1

Explain the main structure of this code:

MergeSort(called with gradle Starter) gives an array to branch to sort. 
Branch splits the array in half ang gives the them to two sorters.
These sorters sort their half and array.
The branch asks each sorter for their lowest entry.
The lower of the two is sent to starter.
This is previous two steps are repeated until both sorters are empty.
Starter recieves each number from branch as a packet and prints it out.

### Question 2

Explain the main advantages and disadvantages of this setup of the distributed algorithm:

#### Advantages
If a system has enough cores these processes can be split into many different sorters that can 
run at the same time.

#### Disadvantages

The main disadvantages of this code are that at least for merge sort it takes much more time to 
split the work and send it than it does to just sort it. This is because merge sort is O(nlog(n)).
Because merge sort is so efficient it does not really make sense to take on so much overhead of 
splitting and sending the arrays to more branches and sorters, because the overhead will be 
more than the savings from splitting the work.

### Question 3 and 4

Run the code with different arrays to sort (different sizes, numbers etc.) and include code to measure the time (you can just enter start and end times). In your
README.md, describe your experiments and your analysis of them. E.g. Why is
the result as it is? Does the distribution help? Why, why not? See this as setting
up your own experiment and give me a good description and evaluation.

#### Experiment setup

For the experiment I used random number generator and a for loop to fill the array with n number
of random integers. I used System.nanoTime() stored in longs start and stop. I took the innitial
start time right before sending the response out and the stop time after the last response is
received. I print out ((stop - start)/nanoSecondsPerMillisecond).

For Test 1 Starter sends the array to one sorter which sorts and returns the array.
For Test 2 Starter sends the array to one branch which sends the left half to one sorter
and the right half to another sorter which sort and return their arrays, which are merged
by branch and sent back to starter.
For Test 3 Starter sends the array to one branch which sends the left half to second branch
and the right half to a third branch. The second and third branches split their portion of the 
array in half and send the right half to a sorter and the left half to another sorter, making
four sorters in total. The four sorters sort and return their arrays to the second and third
branch who merge the 2 arrays from sorter and return the merged arrays to the first branch who
merges them and returns them to starter.

#### Results
##### Test 1

One Sorter

###### Round 1
n = 50
deltat = 32ms

###### Round 2
n = 100
deltat = 59ms

###### Round 3
n = 150
deltat = 88ms

###### Round 4
n = 200
deltat = 100ms

###### Round 5
n = 250
deltat = 115ms



##### Test 2

One branch, two sorters

###### Round 1
n = 50
deltat = 135ms

###### Round 2
n = 100
deltat = 227ms

###### Round 3
n = 150
deltat = 323ms

###### Round 4
n = 200
deltat = 354ms

###### Round 5
n = 250
deltat = 407ms



##### Test 3
3 branches, 4 sorters

###### Round 1
n = 50
deltat = 372ms

###### Round 2
n = 100
deltat = 772ms

###### Round 3
n = 150
deltat = 803ms

###### Round 4
n = 200
deltat = 1075ms

###### Round 5
n = 250
deltat = 1170ms

#### Analysis
The results from test 1 can be fitted to the equation -175+52ln(n).
The results from test 2 can be fitted to the equation -535+169ln(n).
The results from test 3 can be fitted to the equation -1503 +481ln(n).

Test 2 took an average of 3.76 times as long as Test 1.
Test 3 took an average of 10.95 times as long as Test 1.
Test 3 took an average of 2.91 times as long as Test 2.


#### Conclusion

The results take longer the more layers are added, which is what was expected. This is because
merge sort has O(nlog(n)) and so the savings from splitting up the work are much less than the
overhead cost of sending all the data to different sorters and branches. In test 1 we only send
the data twice(once to sorter and once back to starter). In test 2 it takes 3.76 times as long. 
In test 3 it takes 10.95 times as long as test1. For merge sort the distribution does not help, 
because the overhead of sending the data is more than the savings from concurrently running sort on smaller ns. if another sorting 
algorithm was used like selection sort which has O(n^2) then distribution would be helpful and
could greatly reduce the sorting time.

### Question 5

Explain the traffic that you see on Wireshark. How much traffic is generated with
this setup and do you see a way to reduce it?

For an empty list 162 packets are sent on a 1 sorter set up.
For a list of size 5, 218 packets are sent on a 1 sorter set up.
For a list of size 50, 859 packets are sent on a 1 sorter set up.

For an empty list 230 packets are sent on a 1 branch 2 sorter set up.
For a list of size 5, 541 packets are sent on a 1 branch 2 sorter set up.
For a list of size 50,2535 packets are sent on a 1 branch 2 sorter set up.

For an empty list 398 packets are sent on a 3 branch 4 sorter set up.
For a list of size 5, 1015 packets were sent on a 3 branch 4 sorter set up.
For a list of size 50, 6555 packets were sent on a 3 branch 4 sorter set up.

The reason for the huge amount of traffic is that although the initial packet sent from starter
contains the entire list and the branches send the half list all together to the sorters, when
ever the branch merges 2 sorted list it aks the two lower levels to send their lowest and it does
not save the information from the last time, so rather than getting 2 packets, one from each 
lower level, we get 2n the number of packets for each merge level. Then when the sorted list is
sent back to the starter each number is sent individually. We could drastically reduce the number
of packets by sending the entire sorted list up a level and have mergers save it for merging, 
rather than asking for a new packet for each number merged into the list. Sending the full list
to starter could also lower the number of packets. Doing this could greatly reduce the overhead
of sending and receiving packets and lower the time it takes for distributed set ups to sort.

## Task 2

### Question 1

Do you expect changes in runtimes? Why, why not?

Yes, I expect that the run times will be longer because each packet has to travel to the AWS 
server and back. While travel times for internet packets are usually pretty small, there are
a very large amount of packets being sent and a very small change in travel time per packet
might not be negligible.

### Question 2

Do you see a difference in how long it takes to sort the arrays? Explain the differences
(or why there are not differences) with evidence.

RESPONSE

## Task3

### Question 1

Where is the most time lost and could you make this more efficient?

RESPONSE

### Question 2

What would you need to do to make this run distributed or in parallel? (you do not
have to do it only explain)

RESPONSE

### Question 3

Does it make sense to run the algorithm as a distributed algorithm? Why or why
not?

RESPONSE