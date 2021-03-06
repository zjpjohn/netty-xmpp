/**
 * Copyright 2012 José Martínez
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

package es.udc.pfc.xmpp.handler;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.util.CharsetUtil;

import es.udc.pfc.xmpp.component.XMPPComponent;
import es.udc.pfc.xmpp.stanza.IQ;
import es.udc.pfc.xmpp.stanza.Message;
import es.udc.pfc.xmpp.stanza.Presence;
import es.udc.pfc.xmpp.stanza.Stanza;

/**
 * Handles XMPP Stanzas.
 */
public class XMPPStreamHandler extends SimpleChannelHandler {
	
	private final XMPPComponent callback;

	public XMPPStreamHandler(XMPPComponent callback) {
		this.callback = callback;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (!(e.getMessage() instanceof Stanza)) {
			ctx.sendUpstream(e);
			return;
		}

		final Stanza stanza = (Stanza) e.getMessage();
		if (stanza instanceof Message) {
			callback.receivedMessage((Message) stanza);
		} else if (stanza instanceof Presence) {
			callback.receivedPresence((Presence) stanza);
		} else if (stanza instanceof IQ) {
			callback.receivedIQ((IQ) stanza);
		}
	}

	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (!(e.getMessage() instanceof Stanza)) {
			ctx.sendDownstream(e);
			return;
		}

		Channels.write(ctx, e.getFuture(), ChannelBuffers.copiedBuffer(e.getMessage().toString(), CharsetUtil.UTF_8));
	}
	
	public void loggedIn() {
		callback.connected();
	}
	
	@Override
	public void disconnectRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		callback.willDisconnect();

		ctx.sendDownstream(e);
	}
	
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		callback.disconnected();
		
		ctx.sendUpstream(e);
	}
}