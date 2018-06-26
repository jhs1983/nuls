/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.protocol.base.handler;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.message.bus.handler.AbstractMessageHandler;
import io.nuls.network.model.Node;
import io.nuls.protocol.base.cache.ProtocolCacheHandler;
import io.nuls.protocol.base.utils.filter.InventoryFilter;
import io.nuls.protocol.cache.TemporaryCacheManager;
import io.nuls.protocol.message.ForwardTxMessage;
import io.nuls.protocol.message.GetSmallBlockMessage;
import io.nuls.protocol.message.GetTransactionMessage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class ForwardTxMessageHandler extends AbstractMessageHandler<ForwardTxMessage> {

    private static final InventoryFilter inventoryFilter = new InventoryFilter();

    private TemporaryCacheManager cacheManager = TemporaryCacheManager.getInstance();

    @Override
    public void onMessage(ForwardTxMessage message, Node fromNode) {
        if (message == null || fromNode == null || null == message.getMsgBody()) {
            return;
        }
        NulsDigestData hash = message.getMsgBody();
        boolean constains = inventoryFilter.contains(hash.getDigestBytes());
        if (constains) {
            return;
        }

        //todo 某个条件下清空过滤器

        GetTransactionMessage getTransactionMessage = new GetTransactionMessage();
        getTransactionMessage.setMsgBody(hash);
        CompletableFuture<Boolean> future = ProtocolCacheHandler.addGetTxRequest(hash);
        Result result = messageBusService.sendToNode(message, fromNode, false);
        if (result.isFailed()) {
            ProtocolCacheHandler.removeTxFuture(hash);
            return;
        }
        boolean complete;
        try {
            complete = future.get(30L, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.warn("get small block failed:" + hash.getDigestHex(), e);
            return;
        }finally {
            ProtocolCacheHandler.removeTxFuture(hash);
        }
        if (complete) {
            inventoryFilter.insert(hash.getDigestBytes());
        }

    }

}
