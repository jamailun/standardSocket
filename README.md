# --> StandardSocket <--

Short librairie that interface java's tcp protocols. Can be used in client/server java applications.

# How to use it ?

Usage differs if it's server sided or client sided.

## Client side :

### Some theory
Very simple to understand. If you are the client you want :
1) Connect to the hub server, wich gives us a `ServerList`'s instance to know existing virtual servers.
2) Connect to ones of thoses virtual servers.
3) Interact with this virtual server : send and receive packets.

### The real practice
- Create a `JavaClient` instance on your hub server's favorite *(it a joke)* IP address and port `JavaClient client = new JavaClient("127.0.0.1", 8181);`.
- Then listen on what's happening on this client : `client.setListener(new ClientListener() {/* stuff here*/});`.
- In that's listener, you will have some specifics events (see section below) and an other event when you receive a packet.
- Wanna check if the connexion to hub server is successful ? Do `JavaClient#isValid()`.
- You can get the virtual server list with : `JavaClient#getServerList()`.
- You can connect to a virtual server with `JavaClient#startConnectionToVirtualServer()`.
- Wanna send packets to the server ? Get the proxied connection with `JavaClient#getProxySocket()`.

### Events for client side :
- `connectionInitializedWithVS()` : triggers when you enter a virtual server,
- `serverDisconnected()` : triggers when you have been disconnected by the virtual server.
- `serverBroadcastMessage(String)` : triggers when you receives a broadcasted message from virtual server.
- `packetReceived(JavaPacket)` :   **!IMPORTANT!**   triggers when you receive a packet from server. You can do instance checks on the packet and then get informations about it.

## Server side :

### Again : the theory
If you are the server you want :
1) Open a hub server on a specific port.
2) Create some virtual servers clients will be able to connect to.
3) Don't forget to listen to all you virtual servers ! They will necessary to interact with all of yours clients.
4) I have created a Command system. It's very basic you can create your own one with `JavaServer#setCommandExecutor(CommandExecutor)`. Actually my system allows you to add commands when you need to ! Do `JavaServer#usafeGetMultipleCommandExecutor().registerCommand(/*command*/);`. Easy peasy.

### How to do it properly
- Create a `JavaServer` instance on your hub port with `JavaServer server = new JavaServer(8181);`.
- Create some `VirtualServer` instances with `VirtualServer vs = server.addNewVirtualServer("VS-name", 8182, 20);`.
- Listen on thoses virtual servers with `JavaServer#registerListener(new ServerListener() {/*stuff here*/});`.
- Oh ! I almost forget ! Use `JavaServer.start();` to start the whole machine !

### Events for server side :
- `clientConnected(ConnectedSocket)` : triggers when someone connects to the virtual server. **Warning :** it's deprecated to get the client's name at this moment, because clients is not fully identified at this moment.
- `clientDisconnected(ConnectedSocket)` : triggers when someone disconnects from the virtual server.
- `packetReceived(JavaPacket, ConnectedSocket)` :   **!IMPORTANT!**   triggers when you receive a packet from a client. You can do instance checks on the packet and then get informations about it.
- `virtualRoomShutdowns()` : triggers when the virtual server is shutdown. do **not** send `DisconnectPacket` at this point, it's already handled.

# Contact ?
I'm just here on discord : `jamailun#0681`
Or you can mail me : `jamailun@laposte.net`
Sometimes i'm on twitter. Not often : `https://twitter.com/jamailun`
