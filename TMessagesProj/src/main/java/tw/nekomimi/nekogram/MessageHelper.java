package tw.nekomimi.nekogram;

import android.util.SparseArray;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BaseController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import tw.nekomimi.nekogram.utils.AlertUtil;

public class MessageHelper extends BaseController {

    private static SparseArray<MessageHelper> Instance = new SparseArray<>();
    private int lastReqId;

    public MessageHelper(int num) {
        super(num);
    }

    public void resetMessageContent(long dialog_id, MessageObject messageObject) {
        TLRPC.Message message = messageObject.messageOwner;

        MessageObject obj = new MessageObject(currentAccount, message, true, true);

        ArrayList<MessageObject> arrayList = new ArrayList<>();
        arrayList.add(obj);
        getNotificationCenter().postNotificationName(NotificationCenter.replaceMessagesObjects, dialog_id, arrayList, false);
    }

    public static MessageHelper getInstance(int num) {
        MessageHelper localInstance = Instance.get(num);
        if (localInstance == null) {
            synchronized (MessageHelper.class) {
                localInstance = Instance.get(num);
                if (localInstance == null) {
                    Instance.put(num, localInstance = new MessageHelper(num));

                }
            }
        }
        return localInstance;
    }

    public void processForwardFromMyName(ArrayList<MessageObject> messages, long did, boolean notify, int scheduleDate) {
        HashMap<Long, Long> map = new HashMap<>();
        for (int i = 0; i < messages.size(); i++) {
            MessageObject messageObject = messages.get(i);
            ArrayList<TLRPC.MessageEntity> entities;
            if (messageObject.messageOwner.entities != null && !messageObject.messageOwner.entities.isEmpty()) {
                entities = new ArrayList<>();
                for (int a = 0; a < messageObject.messageOwner.entities.size(); a++) {
                    TLRPC.MessageEntity entity = messageObject.messageOwner.entities.get(a);
                    if (entity instanceof TLRPC.TL_messageEntityBold ||
                            entity instanceof TLRPC.TL_messageEntityItalic ||
                            entity instanceof TLRPC.TL_messageEntityPre ||
                            entity instanceof TLRPC.TL_messageEntityCode ||
                            entity instanceof TLRPC.TL_messageEntityTextUrl ||
                            entity instanceof TLRPC.TL_messageEntityStrike ||
                            entity instanceof TLRPC.TL_messageEntityUnderline) {
                        entities.add(entity);
                    }
                    if (entity instanceof TLRPC.TL_messageEntityMentionName) {
                        TLRPC.TL_inputMessageEntityMentionName mention = new TLRPC.TL_inputMessageEntityMentionName();
                        mention.length = entity.length;
                        mention.offset = entity.offset;
                        mention.user_id = getMessagesController().getInputUser(((TLRPC.TL_messageEntityMentionName) entity).user_id);
                        entities.add(mention);
                    }
                }
            } else {
                entities = null;
            }
            if (messageObject.messageOwner.media != null && !(messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaEmpty) && !(messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage) && !(messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGame) && !(messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaInvoice)) {
                HashMap<String, String> params = null;
                if ((int) did == 0 && messageObject.messageOwner.peer_id != null && (messageObject.messageOwner.media.photo instanceof TLRPC.TL_photo || messageObject.messageOwner.media.document instanceof TLRPC.TL_document)) {
                    params = new HashMap<>();
                    params.put("parentObject", "sent_" + messageObject.messageOwner.peer_id.channel_id + "_" + messageObject.getId());
                }
                long oldGroupId = messageObject.messageOwner.grouped_id;
                if (oldGroupId != 0) {
                    if (params == null) {
                        params = new HashMap<>();
                    }
                    Long groupId;
                    if (map.containsKey(oldGroupId)) {
                        groupId = map.get(oldGroupId);
                    } else {
                        groupId = Utilities.random.nextLong();
                        map.put(oldGroupId, groupId);
                    }
                    params.put("groupId", String.valueOf(groupId));
                    if (i == messages.size() - 1) {
                        params.put("final", "true");
                    } else {
                        long nextOldGroupId = messages.get(i + 1).messageOwner.grouped_id;
                        if (nextOldGroupId != oldGroupId) {
                            params.put("final", "true");
                        }
                    }
                }
                if (messageObject.messageOwner.media.photo instanceof TLRPC.TL_photo) {
                    getSendMessagesHelper().sendMessage((TLRPC.TL_photo) messageObject.messageOwner.media.photo, null, did, null, null, messageObject.messageOwner.message, entities, null, params, notify, scheduleDate, 0, messageObject);
                } else if (messageObject.messageOwner.media.document instanceof TLRPC.TL_document) {
                    getSendMessagesHelper().sendMessage((TLRPC.TL_document) messageObject.messageOwner.media.document, null, messageObject.messageOwner.attachPath, did, null, null, messageObject.messageOwner.message, entities, null, params, notify, scheduleDate, 0, messageObject);
                } else if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaVenue || messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaGeo) {
                    getSendMessagesHelper().sendMessage(messageObject.messageOwner.media, did, null, null, null, null, notify, scheduleDate);
                } else if (messageObject.messageOwner.media.phone_number != null) {
                    TLRPC.User user = new TLRPC.TL_userContact_old2();
                    user.phone = messageObject.messageOwner.media.phone_number;
                    user.first_name = messageObject.messageOwner.media.first_name;
                    user.last_name = messageObject.messageOwner.media.last_name;
                    user.id = messageObject.messageOwner.media.user_id;
                    getSendMessagesHelper().sendMessage(user, did, null, null, null, null, notify, scheduleDate);
                } else if ((int) did != 0) {
                    ArrayList<MessageObject> arrayList = new ArrayList<>();
                    arrayList.add(messageObject);
                    getSendMessagesHelper().sendMessage(arrayList, did, notify, scheduleDate);
                }
            } else if (messageObject.messageOwner.message != null) {
                TLRPC.WebPage webPage = null;
                if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage) {
                    webPage = messageObject.messageOwner.media.webpage;
                }
                getSendMessagesHelper().sendMessage(messageObject.messageOwner.message, did, null, null, webPage, webPage != null, entities, null, null, notify, scheduleDate);
            } else if ((int) did != 0) {
                ArrayList<MessageObject> arrayList = new ArrayList<>();
                arrayList.add(messageObject);
                getSendMessagesHelper().sendMessage(arrayList, did, notify, scheduleDate);
            }
        }
    }

    public void deleteUserChannelHistoryWithSearch(final long dialog_id, final TLRPC.User user) {
        deleteUserChannelHistoryWithSearch(dialog_id, user, 0);
    }

    public void deleteUserChannelHistoryWithSearch(final long dialog_id, final TLRPC.User user, final int offset_id) {
        final TLRPC.TL_messages_search req = new TLRPC.TL_messages_search();
        req.peer = getMessagesController().getInputPeer((int) dialog_id);
        if (req.peer == null) {
            return;
        }
        req.limit = 100;
        req.q = "";
        req.offset_id = offset_id;
        if (user != null) {
            req.from_id = MessagesController.getInputPeer(user);
            req.flags |= 1;
        }
        req.filter = new TLRPC.TL_inputMessagesFilterEmpty();
        final int currentReqId = ++lastReqId;
        getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                int lastMessageId = offset_id;
                if (currentReqId == lastReqId) {
                    if (response != null) {
                        TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;
                        int size = res.messages.size();
                        if (size == 0) {
                            return;
                        }
                        ArrayList<Integer> ids = new ArrayList<>();
                        ArrayList<Long> random_ids = new ArrayList<>();
                        int channelId = 0;
                        for (int a = 0; a < res.messages.size(); a++) {
                            TLRPC.Message message = res.messages.get(a);
                            ids.add(message.id);
                            if (message.random_id != 0) {
                                random_ids.add(message.random_id);
                            }
                            if (message.peer_id.channel_id != 0) {
                                channelId = message.peer_id.channel_id;
                            }
                            if (message.id > lastMessageId) {
                                lastMessageId = message.id;
                            }
                        }
                        getMessagesController().deleteMessages(ids, random_ids, null, dialog_id, channelId, true, false);
                        deleteUserChannelHistoryWithSearch(dialog_id, user, lastMessageId);
                    }
                }
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    public void deleteChannelHistory(final long dialog_id, TLRPC.Chat chat, final int offset_id) {

        final TLRPC.TL_messages_getHistory req = new TLRPC.TL_messages_getHistory();
        req.peer = getMessagesController().getInputPeer((int) dialog_id);
        if (req.peer == null) {
            return;
        }
        req.limit = 100;
        req.offset_id = offset_id;
        final int currentReqId = ++lastReqId;
        getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                int lastMessageId = offset_id;
                if (currentReqId == lastReqId) {
                    if (response != null) {
                        TLRPC.messages_Messages res = (TLRPC.messages_Messages) response;
                        int size = res.messages.size();
                        if (size == 0) {
                            return;
                        }
                        /*
                        ArrayList<Integer> ids = new ArrayList<>();
                        ArrayList<Long> random_ids = new ArrayList<>();
                        int channelId = 0;
                        for (int a = 0; a < res.messages.size(); a++) {
                            TLRPC.Message message = res.messages.get(a);
                            ids.add(message.id);
                            if (message.random_id != 0) {
                                random_ids.add(message.random_id);
                            }
                            if (message.to_id.channel_id != 0) {
                                channelId = message.to_id.channel_id;
                            }
                            if (message.id > lastMessageId) {
                                lastMessageId = message.id;
                            }
                        }
                        getMessagesController().deleteMessages(ids, random_ids, null, dialog_id, channelId, true, false);
                         */
                        HashSet<Integer> ids = new HashSet<>();
                        ArrayList<Integer> msgIds = new ArrayList<>();
                        ArrayList<Long> random_ids = new ArrayList<>();
                        int channelId = 0;
                        for (int a = 0; a < res.messages.size(); a++) {
                            TLRPC.Message message = res.messages.get(a);
                            ids.add(message.id);
                            if (message.from_id.user_id > 0) {
                                ids.add(message.peer_id.user_id);
                            } else {
                                msgIds.add(message.id);
                                if (message.random_id != 0) {
                                    random_ids.add(message.random_id);
                                }
                            }
                            if (message.id > lastMessageId) {
                                lastMessageId = message.id;
                            }
                        }
                        for (int userId : ids) {
                            deleteUserChannelHistory(chat, userId, 0);
                        }
                        if (!msgIds.isEmpty()) {
                            getMessagesController().deleteMessages(msgIds, random_ids, null, dialog_id, channelId, true, false);
                        }
                        deleteChannelHistory(dialog_id, chat, lastMessageId);

                    }
                }
            } else {
                AlertUtil.showToast(error.code + ": " + error.text);
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    public void deleteUserChannelHistory(final TLRPC.Chat chat, int userId, int offset) {
        if (offset == 0) {
            getMessagesStorage().deleteUserChatHistory(chat.id, chat.id, userId);
        }
        TLRPC.TL_channels_deleteUserHistory req = new TLRPC.TL_channels_deleteUserHistory();
        req.channel = getMessagesController().getInputChannel(chat.id);
        req.user_id = getMessagesController().getInputUser(userId);
        getConnectionsManager().sendRequest(req, (response, error) -> {
            if (error == null) {
                TLRPC.TL_messages_affectedHistory res = (TLRPC.TL_messages_affectedHistory) response;
                if (res.offset > 0) {
                    deleteUserChannelHistory(chat, userId, res.offset);
                }
                getMessagesController().processNewChannelDifferenceParams(res.pts, res.pts_count, chat.id);
            }
        });
    }

}
