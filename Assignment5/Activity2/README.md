# Activity 2

## Screencast

https://youtu.be/hcLgLSoO9LU


## How to run
run all on local host no port or host is required
NODES MUST BE RUN FIRST
LEADER MUST BE RUN BEFORE CLIENT

run node1:
	gradle node1
for custom money node1:
	gradle node1 -Pmoney=#

run node2:
	gradle node2
for custom money node2:
	gradle node2 -Pmoney=#

Custom money does not change money if node already has ledger, amount on ledger will be applied.

run leader:
	gradle leader

run Client
	gradle client

## Description

The program consists of Client.java, Leader.java, Performer.java, Nodes.java, Nodes.java, NetworkUitils.java, JsonUtils.java, and a build.gradle file



The program runs exactly two nodes, node1 on port 8001, node2 on port 8002

The program runs exactly one leader on port 8000, which creates a performer for each client which connects.

The program can run as many clients as it wants.



When the nodes are started they check if they have a ledger file if they do, they update the nodes available money to what is in the ledger.

When the leader is started it asks the nodes for their ledgers and copies them into its own.

	The assumption here is that when a node updates it's ledger the client receives the credit or the payment is applied so the node ledgers
	are the correct ledgers.



When the client is started it connects to the leader.

The leader then asks for the client id.

The client gets the id from the user and sends it to the leader.

The leader sends the client a greeting.

The client goes to the main menu.



If the user selects 1, the client will go to credit, then ask how much credit and send that amount to the leader in a credit request.

The leader will send each node the client id and the requested amount.

The nodes will check that the client has no credit with them and check that the node has 1.5x the amount and will either accept or deny the request based on
this criteria.

If both nodes accept the request the leader will ask the nodes to apply the credit and will send the client an acceptance message.

If either node denies the request the leader will send the client a refusal message.



If the user selects 2, the client will go to payback, then ask how much the client wants to payback and send that amount to the leader in a payback request.

The leader will send the id and the amount(calculated for each node based on which node has more money)

The node will check that the client does have credit with them and that the amount to be payed back per node is less than or equal to the amount owed.

If the criteria is met the nodes will accept the request.

If both nodes accept the request the leader will split up the amount and ask the nodes to apply the payback and a success message will be sent to the client.

If only one node accepts the full payback amount will be applied to that node and a success message will be sent to the client.

If neither node accepts a refusal message will be sent to the client.



If the user selects 0, the client will send a good bye message to the leader and wait for a response.

The leader will send a goodbye message which the client will print out and both will terminate.(the leader terminates that client's performer thread.



If either node is down when a client makes a response, nothing else will crash, the leader will just send an error message to the client asking them to
try again later.

## Requirements

	1. I have done the readme requirement
	2. The project is well structured and easy to understand(I did my best)
	3. the leader is run through gradle leader with a default port of 8000
	4. at least two seperate nodes are ran through gradle node1 and gradle node2(nodes are servers to the leader)
	5. Nodes start with initial money of 100, gradle node1 -Pmoney=# is used to set custom money(will not work if the node 
	already has a ledger with credits given, because do you want inflation, thats how you get inflation)
	6. Leader asks client for their client id
	8. client will have the choice to 1: apply for credit, 2: request payback of credit, or 0: quit
	9.The leader recieves the request with the amount.
	10. credit is successfully applied when client does not have credit with either node and the nodes have 1.5x the amount 
	requested.
	11. payback is successfully applied when the client is trying to payback less than or equal the amount owed to either or
	both nodes.
	12. if a node crashes, the leader, the other node, and all clients stay intact. If a client tries to make a request while 
	the node is down the leader sends
	an error message.
	13. if a restart is needed the leader check with the nodes and updates its ledger accordingly(assumption is nodes are right 
	and have the money)
	14. If multiple clients make requests whoever gets the lock first gets to make the first request(in order of request instead 
	of in order of login, because
	what if a client logs in then justs walks away)
	15. Nope. only two nodes. I am tired.

## Protocol
The protocol is split up into two sets, one for leader and client dialog, and another for node to leader dialog.

### Protocol for nodes

The leader initiates all node-leader interactions.

The leader must send "method"

Some methods require "ID" and "amount

method:

	canPayback:
		Calls node method canPayback(json)
		requires "ID" and "amount"
		returns "method" ["can payback" or "can not payback"]

	canCredit:
		Calls node method canCredit(json)
		requires "ID" and "amount"
		returns "method" ["can credit" or "can not credit"]

	payback:
		Calls node method payback(json)
		requires "ID" and "amount"
		returns "method" "payback"

	credit:
		Calls node method credit(json)
		requires "ID" and "amount"
		returns "method" "credit"

	verify:
		Calls node method verify(json)
		returns "method" "verify"
		    and "ledger" <string containing ledger>

Leader uses NetworkUtils.send(String host, int port, JSONObject message) to send requests to nodes.

Nodes use NetworkUtils.respond(Socket conn, JSONObject message) to respond to leader requests.

### Protocol for clients
JsonUtils.fromByteArray(byte[] bytes) is used to convert byte arrays to JSONObjects

JsonUtils.toByteArray(JSONObject object) is used to conver JSONObjects to byte arrays


Clients use NetworkUtils.sendClient(OutputStream out, byte... bytes) to send to leader.

Clients use NetworkUtils.readClient(InputStream in, int length) to read in from leader.


Leaders use NetworkUtils.sendClient(OutputStream out, byte... bytes) to send to clients.

Leaders use NetworkUtils.readClient(InputStream in, int length) to read in from clients.


message pair types:

	"ID" <string holding either ID or ID request>;
	"hello" <string holding greeting>;
	"credit" <int amount requested> or <String response to request>;
	"payback" <int amount requested> or <String response to request>;
	"exit" <String goodbye message>;
	"error" <String error message>;
