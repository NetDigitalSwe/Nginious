/**
 * Copyright 2012 NetDigital Sweden AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.nginious.http.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles reading and writing of data for a server connection. Instances are created by
 * {@link Server} for each incoming connection.
 * 
 * <p>
 * This class is typically subclassed to implement protocol specific functionality.
 * </p>
 * 
 * @author Bojan Pisler, NetDigital Sweden AB
 *
 */
public abstract class Connection {
	
	private Server server;
	
	private SocketChannel channel;
	
	private InetSocketAddress remoteAddress;
	
	private SelectionKey key;
	
	private boolean closed;
	
	private boolean reading;
	
	private ConcurrentLinkedQueue<ByteBuffer> pendingWrites;
	
	private Connection pendingSwitchConnection;
	
	/**
	 * Constructs a new empty connection.
	 */
	protected Connection() {
		this.pendingWrites = new ConcurrentLinkedQueue<ByteBuffer>();
		this.closed = false;
	}
	
	/**
	 * Constructs a new connection for the specified server with the underlying channel and selection key.
	 * 
	 * @param server the server
	 * @param channel the connections socket channel for reading and writing data
	 * @param key the socket channels selection key
	 * @throws IOException if unable to create connection
	 */
	protected Connection(Server server, SocketChannel channel, SelectionKey key) throws IOException {
		this();
		this.server = server;
		this.channel = channel;
		this.remoteAddress = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
		this.key = key;
	}
	
	/**
	 * Returns the server for this connection.
	 * 
	 * @return the server
	 */
	Server getServer() {
		return this.server;
	}
	
	/**
	 * Sets the server for this connection to the specified server.
	 * 
	 * @param server the server
	 */
	protected void setServer(Server server) {
		this.server = server;
	}
	
	/**
	 * Returns the underlying socket channel for this connection.
	 * 
	 * @return the channel
	 */
	SocketChannel getChannel() {
		return this.channel;
	}
	
	/**
	 * Sets the socket channel to the specified channel for this connection.
	 * 
	 * @param channel the channel
	 * @throws IOException if an I/O error occurs.
	 */
	protected void setChannel(SocketChannel channel) throws IOException {
		this.channel = channel;
		this.remoteAddress = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
	}
	
	/**
	 * Returns selection key for this connections socket channel.
	 * 
	 * @return the selection key
	 */
	SelectionKey getSelectionKey() {
		return this.key;
	}
	
	/**
	 * Sets selection key to the specified key for this connection.
	 * 
	 * @param key the selection key
	 */
	void setSelectionKey(SelectionKey key) {
		this.key = key;
	}
	
	/**
	 * Handles handover of underlying connection properties from this connection to the
	 * specified connection. Properties include server, socket channel and selection key.
	 * This method is typically used when handing over from a connection implementing one
	 * protocol to another connection implementing another protocol
	 * 
	 * <p>
	 * A typical example is the handover in the WebSocket protocol where HTTP is used in
	 * the initial handshake step.
	 * </p>
	 * 
	 * @param conn the connectionto handover to
	 * @throws IOException if an I/O error occurs
	 */
	public void switchConnection(Connection conn) throws IOException {
		conn.setServer(getServer());
		conn.setChannel(getChannel());
		conn.setSelectionKey(getSelectionKey());
		this.pendingSwitchConnection = conn;
	}
	
	/**
	 * Returns remote address of client connected to this connection.
	 * 
	 * @return the remote address.
	 */
	public String getRemoteAddress() {
		return remoteAddress.getAddress().getHostAddress();
	}
	
	/**
	 * Returns remote host of client connected to this connection.
	 * 
	 * @return the remote host
	 */
	public String getRemoteHost() {
		return remoteAddress.getHostName();
	}
	
	/**
	 * Returns remote port of client connected to this connection.
	 * 
	 * @return the remote port
	 */
	public int getRemotePort() {
		return remoteAddress.getPort();
	}
	
	/**
	 * Called by server to let connection write data to the underlying socket channel. This
	 * method is called as a result of a previous call to {@link Server#queueWrite(Connection)}.
	 * 
	 * <p>
	 * This method writes data to the socket channel for as long as there is data to write. Once
	 * there is no more data to write the method either calls {@link Server#queueClose(Connection)}
	 * if the close flag is raised or {@link Server#queueRead(Connection)} to inform server to
	 * read data for this connection.
	 * </p>
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	protected void write() throws IOException {
		boolean write = true;
		
		while(write && !pendingWrites.isEmpty()) {
			ByteBuffer buff = pendingWrites.peek();
			channel.write(buff);
			
			if(buff.remaining() == 0) {
				pendingWrites.poll();
				break;
			} else {
				write = false;
			}
		}
		
		if(pendingWrites.isEmpty()) {
			if(this.closed) {
				key.interestOps(0);
				key.cancel();
				channel.close();
			} else {
				if(this.pendingSwitchConnection != null) {
					key.attach(this.pendingSwitchConnection);
					server.queueRead(this.pendingSwitchConnection);
					this.pendingSwitchConnection = null;
				} else if(this.reading) {
					server.queueRead(this);
				}
			}
		}
	}
	
	/**
	 * Adds the specified buffer to this connections pending writes and calls
	 * server to queue write operation.
	 * 
	 * @param buffer the data to write
	 * @throws IOException if unable to queue write
	 */
	public void queueWrite(ByteBuffer buffer) throws IOException {
		this.reading = false;
		pendingWrites.add(buffer);
		server.queueWrite(this);
	}
	
	/**
	 * Called by server when data is available for reading from underlying socket channel.
	 * 
	 * @throws IOException if an I/O error occurs while reading data.
	 */
	void read() throws IOException {
		read(this.channel);
	}
	
	/**
	 * Reads data from the specified socket channel.
	 * 
	 * @param channel the socket channel to read data from
	 * @throws IOException if an I/O error occurs while reading data from the channel
	 */
	protected abstract void read(SocketChannel channel) throws IOException;
	
	/**
	 * Returns whether or not this connection has timed out. This method must be implemented
	 * by connection subclasses implementing protocol specific functionality.
	 * 
	 * @return whether or not this connection has timed out
	 */
	protected abstract boolean isTimedOut();
	
	/**
	 * Performs necessary cleanup on a timed out connection.
	 */
	protected abstract void timedOut();
	
	/**
	 * Switches this connection back to reading mode after one or more write operations.
	 */
	public void switchToRead() {
		this.reading = true;
	}
	
	/**
	 * Raises the close flag and queues a close operation with the server unless there is
	 * data that remains to be written to the channel. If there is data to be written
	 * the {@link #write()} method will close the connection as a result of the raised
	 * close flag once all data is written.
	 */
	public void close() {
		close(false);
	}
	
	/**
	 * Raises the close flag and queues a close operation with the server. The write
	 * queue is optionally flushed.
	 * 
	 * @param flushPendingWrites <code>true</code> if write queue should be flushed, <code>false</code> otherwise
	 */
	protected void close(boolean flushPendingWrites) {
		// Raise close flag and let server close on next write operation
		this.closed = true;
		
		if(flushPendingWrites) {
			pendingWrites.clear();
		}
		
		if(pendingWrites.isEmpty()) {
			server.queueClose(this);
		}
	}
}
