package com.uliaovideo.chat.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.uliaovideo.chat.adapter.ChatAdapter;
import com.uliaovideo.chat.model.CustomMessage;
import com.uliaovideo.chat.model.FileMessage;
import com.uliaovideo.chat.model.ImageMessage;
import com.uliaovideo.chat.model.Message;
import com.uliaovideo.chat.model.MessageFactory;
import com.uliaovideo.chat.model.TextMessage;
import com.uliaovideo.chat.model.UGCMessage;
import com.uliaovideo.chat.model.VideoMessage;
import com.uliaovideo.chat.model.VoiceMessage;
import com.uliaovideo.chat.utils.FileUtil;
import com.uliaovideo.chat.utils.MediaUtil;
import com.uliaovideo.chat.utils.RecorderUtil;
import com.uliaovideo.videoline.LiveConstant;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.api.Api;
import com.uliaovideo.videoline.base.BaseActivity;
import com.uliaovideo.videoline.dialog.CuckooRewardCoinDialog;
import com.uliaovideo.videoline.dialog.GiftBottomDialog;
import com.uliaovideo.videoline.event.BaseEvent;
import com.uliaovideo.videoline.event.EventChatClickPrivateImgMessage;
import com.uliaovideo.videoline.json.JsonRequestBase;
import com.uliaovideo.videoline.json.JsonRequestDoPrivateSendGif;
import com.uliaovideo.videoline.json.JsonRequestPrivateChatPay;
import com.uliaovideo.videoline.manage.RequestConfig;
import com.uliaovideo.videoline.manage.SaveData;
import com.uliaovideo.videoline.modle.ConfigModel;
import com.uliaovideo.videoline.modle.custommsg.CustomMsgPrivateGift;
import com.uliaovideo.videoline.modle.custommsg.CustomMsgPrivatePhoto;
import com.uliaovideo.videoline.ui.HomePageActivity;
import com.uliaovideo.videoline.ui.PrivatePhotoActivity;
import com.uliaovideo.videoline.ui.common.Common;
import com.uliaovideo.videoline.utils.DialogHelp;
import com.uliaovideo.videoline.utils.StringUtils;
import com.uliaovideo.videoline.utils.Utils;
import com.lzy.okgo.callback.StringCallback;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMMessageStatus;
import com.tencent.imsdk.ext.message.TIMMessageDraft;
import com.tencent.imsdk.ext.message.TIMMessageExt;
import com.tencent.imsdk.ext.message.TIMMessageLocator;
import com.tencent.qcloud.presentation.presenter.ChatPresenter;
import com.tencent.qcloud.presentation.viewfeatures.ChatView;
import com.tencent.qcloud.ui.ChatInput;
import com.tencent.qcloud.ui.TemplateTitle;
import com.tencent.qcloud.ui.VoiceSendingView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class ChatActivity extends BaseActivity implements ChatView, View.OnClickListener, GiftBottomDialog.DoSendGiftListen {

    private static final String TAG = "ChatActivity";

    private List<Message> messageList = new ArrayList<>();
    private ChatAdapter adapter;
    private ListView listView;
    private ChatPresenter presenter;
    private ChatInput input;
    private ImageView mIvPrivateChat, mIvGift, mIvVideo;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int IMAGE_STORE = 200;
    private static final int FILE_CODE = 300;
    private static final int IMAGE_PREVIEW = 400;
    private static final int VIDEO_RECORD = 500;

    public static final int RESULT_SELECT_PRIVATE_PHOTO = 0x11;
    private Uri fileUri;
    private VoiceSendingView voiceSendingView;
    private String identify;
    private String userName;
    private String avatar;
    private RecorderUtil recorder = new RecorderUtil();
    private TIMConversationType type;
    private String titleStr;
    private GiftBottomDialog giftBottomDialog;

    private int isPay;
    private String payCoin;

    public static void navToChat(Context context, String identify, String userName, String avatar, int isPay, String payCoin, TIMConversationType type) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("identify", identify);
        intent.putExtra("user_nickname", userName);
        intent.putExtra("avatar", avatar);
        intent.putExtra("type", type);
        intent.putExtra("is_pay", isPay);
        intent.putExtra("pay_coin", payCoin);
        context.startActivity(intent);
    }


    @Override
    protected Context getNowContext() {
        return this;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initView() {

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        identify = getIntent().getStringExtra("identify");
        userName = getIntent().getStringExtra("user_nickname");
        avatar = getIntent().getStringExtra("avatar");
        isPay = getIntent().getIntExtra("is_pay", 0);
        payCoin = getIntent().getStringExtra("pay_coin");

        //TIMManagerExt.getInstance().deleteConversationAndLocalMsgs(TIMConversationType.C2C,identify);
        type = (TIMConversationType) getIntent().getSerializableExtra("type");
        presenter = new ChatPresenter(this, identify, type);

        mIvPrivateChat = findViewById(R.id.iv_private_img);
        mIvGift = findViewById(R.id.iv_gift);
        mIvVideo = findViewById(R.id.iv_video);
        findViewById(R.id.iv_reward).setOnClickListener(this);
        mIvPrivateChat.setOnClickListener(this);
        mIvGift.setOnClickListener(this);
        mIvVideo.setOnClickListener(this);

        input = (ChatInput) findViewById(R.id.input_panel);
        input.setChatView(this);
        adapter = new ChatAdapter(this, R.layout.item_message, messageList);
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        input.setInputMode(ChatInput.InputMode.NONE);
                        break;
                }
                return false;
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            private int firstItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && firstItem == 0) {
                    //如果拉到顶端读取更多消息
                    presenter.getMessage(messageList.size() > 0 ? messageList.get(0).getMessage() : null);

                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                firstItem = firstVisibleItem;
            }
        });
        registerForContextMenu(listView);
        TemplateTitle title = (TemplateTitle) findViewById(R.id.chat_title);

        switch (type) {
            case C2C:
                title.setMoreImg(R.drawable.btn_person);
                title.setMoreImgAction(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Common.jumpUserPage(ChatActivity.this, identify);
                    }
                });
                title.setTitleText(userName);
                break;

        }
        voiceSendingView = (VoiceSendingView) findViewById(R.id.voice_sending);
        presenter.start();
    }

    @Override
    protected void initSet() {

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initPlayerDisplayData() {

    }


    @Override
    protected void onPause() {
        super.onPause();
        //退出聊天界面时输入框有内容，保存草稿
        if (input.getText().length() > 0) {
            TextMessage message = new TextMessage(input.getText());
            presenter.saveDraft(message.getMessage());
        } else {
            presenter.saveDraft(null);
        }
//        RefreshEvent.getInstance().onRefresh();
        presenter.readMessages();
        MediaUtil.getInstance().stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.stop();
    }


    /**
     * 显示消息
     *
     * @param message
     */
    @Override
    public void showMessage(TIMMessage message) {
        if (message == null) {
            adapter.notifyDataSetChanged();
        } else {
            Message mMessage = MessageFactory.getMessage(message);
            if (mMessage != null) {
                if (messageList.size() == 0) {
                    mMessage.setHasTime(null);
                } else {
                    mMessage.setHasTime(messageList.get(messageList.size() - 1).getMessage());
                }
                messageList.add(mMessage);
                adapter.notifyDataSetChanged();
                listView.setSelection(adapter.getCount() - 1);

            }
        }

    }

    /**
     * 显示消息
     *
     * @param messages
     */
    @Override
    public void showMessage(List<TIMMessage> messages) {
        int newMsgNum = 0;
        for (int i = 0; i < messages.size(); ++i) {
            Message mMessage = MessageFactory.getMessage(messages.get(i));
            if (mMessage == null || messages.get(i).status() == TIMMessageStatus.HasDeleted) {
                continue;
            }
            /*if (mMessage instanceof CustomMessage && LiveConstant.mapCustomMsgClass.get(((CustomMessage)mMessage).getType()) == null){
                continue;
            }*/
            ++newMsgNum;
            if (i != messages.size() - 1) {
                mMessage.setHasTime(messages.get(i + 1));
                messageList.add(0, mMessage);
            } else {
                mMessage.setHasTime(null);
                messageList.add(0, mMessage);
            }
        }
        adapter.notifyDataSetChanged();
        listView.setSelection(newMsgNum);
    }

    @Override
    public void showRevokeMessage(TIMMessageLocator timMessageLocator) {
        for (Message msg : messageList) {
            TIMMessageExt ext = new TIMMessageExt(msg.getMessage());
            if (ext.checkEquals(timMessageLocator)) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 清除所有消息，等待刷新
     */
    @Override
    public void clearAllMessage() {
        messageList.clear();
    }

    /**
     * 发送消息成功
     *
     * @param message 返回的消息
     */
    @Override
    public void onSendMessageSuccess(TIMMessage message) {
        showMessage(message);
    }

    /**
     * 发送消息失败
     *
     * @param code 返回码
     * @param desc 返回描述
     */
    @Override
    public void onSendMessageFail(int code, String desc, TIMMessage message) {
        long id = message.getMsgUniqueId();
        for (Message msg : messageList) {
            if (msg.getMessage().getMsgUniqueId() == id) {
                switch (code) {
                    case 80001:
                        //发送内容包含敏感词
                        msg.setDesc(getString(R.string.chat_content_bad));
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        }

        adapter.notifyDataSetChanged();

    }

    /**
     * 发送图片消息
     */
    @Override
    public void sendImage() {
        Intent intent_album = new Intent("android.intent.action.GET_CONTENT");
        intent_album.setType("image/*");
        startActivityForResult(intent_album, IMAGE_STORE);
    }

    /**
     * 发送照片消息
     */
    @Override
    public void sendPhoto() {
        Intent intent_photo = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent_photo.resolveActivity(getPackageManager()) != null) {
            File tempFile = FileUtil.getTempFile(FileUtil.FileType.IMG);
            if (tempFile != null) {
                fileUri = Uri.fromFile(tempFile);
            }
            intent_photo.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent_photo, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    //发送私照
    private void sendPrivateImg(String imgId, String imgUrl) {

        CustomMsgPrivatePhoto img = new CustomMsgPrivatePhoto();
        img.setId(imgId);
        img.setImg(imgUrl);

        Message message = new CustomMessage(img, LiveConstant.CustomMsgType.MSG_PRIVATE_GIFT);
        presenter.sendMessage(message.getMessage());
    }

    /**
     * 发送文本消息
     */
    @Override
    public void sendText() {

        if (TextUtils.isEmpty(input.getText())) {
            showToast("发送内容不能为空!");
            return;
        }

        if (!Utils.dirtyWordFilter(input.getText().toString())) {
            showToast("发送内容包含敏感词汇!");
            return;
        }

        if (isPay == 1 && StringUtils.toInt(payCoin) != 0) {

            DialogHelp.getConfirmDialog(this, "是否花费" + payCoin + RequestConfig.getConfigObj().getCurrency() + "发送付费消息？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    Api.doRequestChatPay(SaveData.getInstance().getId(), SaveData.getInstance().getToken(), identify, new StringCallback() {
                        @Override
                        public void onSuccess(String s, Call call, Response response) {

                            JsonRequestPrivateChatPay data = (JsonRequestPrivateChatPay) JsonRequestBase.getJsonObj(s, JsonRequestPrivateChatPay.class);
                            if (data.getCode() == 1) {

                                Message message = new TextMessage(input.getText());
                                presenter.sendMessage(message.getMessage());
                                input.setText("");
                            } else {
                                ToastUtils.showShort(data.getMsg());
                            }
                        }
                    });
                }
            }).show();

            return;
        }

        Message message = new TextMessage(input.getText());
        presenter.sendMessage(message.getMessage());
        input.setText("");
    }

    /**
     * 发送文件
     */
    @Override
    public void sendFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_CODE);
    }


    /**
     * 开始发送语音消息
     */
    @Override
    public void startSendVoice() {

        voiceSendingView.setVisibility(View.VISIBLE);
        voiceSendingView.showRecording();
        recorder.startRecording();

    }

    /**
     * 结束发送语音消息
     */
    @Override
    public void endSendVoice() {
        voiceSendingView.release();
        voiceSendingView.setVisibility(View.GONE);
        recorder.stopRecording();
        if (recorder.getTimeInterval() < 1) {
            Toast.makeText(this, getResources().getString(R.string.chat_audio_too_short), Toast.LENGTH_SHORT).show();
        } else if (recorder.getTimeInterval() > 60) {
            Toast.makeText(this, getResources().getString(R.string.chat_audio_too_long), Toast.LENGTH_SHORT).show();
        } else {

            if (isPay == 1 && StringUtils.toInt(payCoin) != 0) {
                DialogHelp.getConfirmDialog(this, "是否花费" + payCoin + RequestConfig.getConfigObj().getCurrency() + "发送付费消息？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Api.doRequestChatPay(SaveData.getInstance().getId(), SaveData.getInstance().getToken(), identify, new StringCallback() {
                            @Override
                            public void onSuccess(String s, Call call, Response response) {

                                JsonRequestPrivateChatPay data = (JsonRequestPrivateChatPay) JsonRequestBase.getJsonObj(s, JsonRequestPrivateChatPay.class);
                                if (data.getCode() == 1) {

                                    Message message = new VoiceMessage(recorder.getTimeInterval(), recorder.getFilePath());
                                    presenter.sendMessage(message.getMessage());
                                } else {
                                    ToastUtils.showShort(data.getMsg());
                                }
                            }
                        });
                    }
                }).show();
                return;
            }

            Message message = new VoiceMessage(recorder.getTimeInterval(), recorder.getFilePath());
            presenter.sendMessage(message.getMessage());
        }
    }

    /**
     * 发送小视频消息
     *
     * @param fileName 文件名
     */
    @Override
    public void sendVideo(String fileName) {
        Message message = new VideoMessage(fileName);
        presenter.sendMessage(message.getMessage());
    }


    /**
     * 结束发送语音消息
     */
    @Override
    public void cancelSendVoice() {

    }

    /**
     * 正在发送
     */
    @Override
    public void sending() {
        if (type == TIMConversationType.C2C) {
            //Message message = new CustomMessage(CustomMessage.Type.TYPING);
            //presenter.sendOnlineMessage(message.getMessage());
        }
    }

    /**
     * 显示草稿
     */
    @Override
    public void showDraft(TIMMessageDraft draft) {
        input.getText().append(TextMessage.getString(draft.getElems(), this));
    }

    @Override
    public void videoAction() {

        // TODO 录制视频
        //Intent intent = new Intent(this, TCVideoRecordActivity.class);
        //startActivityForResult(intent, VIDEO_RECORD);
    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Message message = messageList.get(info.position);
        menu.add(0, 1, Menu.NONE, getString(R.string.chat_del));
        if (message.isSendFail()) {
            menu.add(0, 2, Menu.NONE, getString(R.string.chat_resend));
        } else if (message.getMessage().isSelf()) {
            menu.add(0, 4, Menu.NONE, getString(R.string.chat_pullback));
        }
        if (message instanceof ImageMessage || message instanceof FileMessage) {
            menu.add(0, 3, Menu.NONE, getString(R.string.chat_save));
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Message message = messageList.get(info.position);
        switch (item.getItemId()) {
            case 1:
                message.remove();
                messageList.remove(info.position);
                adapter.notifyDataSetChanged();
                break;
            case 2:
                messageList.remove(message);
                presenter.sendMessage(message.getMessage());
                break;
            case 3:
                message.save();
                break;
            case 4:
                presenter.revokeMessage(message.getMessage());
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK && fileUri != null) {
                showImagePreview(fileUri.getPath());
            }
        } else if (requestCode == IMAGE_STORE) {
            if (resultCode == RESULT_OK && data != null) {
                showImagePreview(FileUtil.getFilePath(this, data.getData()));
            }

        } else if (requestCode == FILE_CODE) {
            if (resultCode == RESULT_OK) {
                sendFile(FileUtil.getFilePath(this, data.getData()));
            }
        } else if (requestCode == IMAGE_PREVIEW) {
            if (resultCode == RESULT_OK) {
                boolean isOri = data.getBooleanExtra("isOri", false);
                String path = data.getStringExtra("path");
                File file = new File(path);
                if (file.exists()) {
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(path, options);
                    if (file.length() == 0 && options.outWidth == 0) {
                        Toast.makeText(this, getString(R.string.chat_file_not_exist), Toast.LENGTH_SHORT).show();
                    } else {
                        if (file.length() > 1024 * 1024 * 10) {
                            Toast.makeText(this, getString(R.string.chat_file_too_large), Toast.LENGTH_SHORT).show();
                        } else {
                            Message message = new ImageMessage(path, isOri);
                            presenter.sendMessage(message.getMessage());
                        }
                    }
                } else {
                    Toast.makeText(this, getString(R.string.chat_file_not_exist), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == VIDEO_RECORD) {
            if (resultCode == RESULT_OK) {
                String videoPath = data.getStringExtra("videoPath");
                String coverPath = data.getStringExtra("coverPath");
                long duration = data.getLongExtra("duration", 0);
                Message message = new UGCMessage(videoPath, coverPath, duration);
                presenter.sendMessage(message.getMessage());
            }
        } else if (requestCode == RESULT_SELECT_PRIVATE_PHOTO) {
            if (resultCode == RESULT_OK) {
                sendPrivateImg(data.getStringExtra("img_id"), data.getStringExtra("img_url"));
            }
        }

    }


    private void showImagePreview(String path) {
        if (path == null) return;
        Intent intent = new Intent(this, ImagePreviewActivity.class);
        intent.putExtra("path", path);
        startActivityForResult(intent, IMAGE_PREVIEW);
    }

    private void sendFile(String path) {
        if (path == null) return;
        File file = new File(path);
        if (file.exists()) {
            if (file.length() > 1024 * 1024 * 10) {
                Toast.makeText(this, getString(R.string.chat_file_too_large), Toast.LENGTH_SHORT).show();
            } else {
                Message message = new FileMessage(path);
                presenter.sendMessage(message.getMessage());
            }
        } else {
            Toast.makeText(this, getString(R.string.chat_file_not_exist), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 将标题设置为对象名称
     */
    private Runnable resetTitle = new Runnable() {
        @Override
        public void run() {
            TemplateTitle title = (TemplateTitle) findViewById(R.id.chat_title);
            title.setTitleText(titleStr);
        }
    };


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.iv_private_img:
                //选择私照发送
                clickSelectPrivatePhoto();
                break;
            case R.id.iv_video:
                Common.callVideo(this, identify);
                break;
            case R.id.iv_gift:
                clickShowGift();
                break;
            case R.id.iv_reward:
                clickReward();
                break;
            default:
                break;
        }
    }

    //点击打赏礼物
    private void clickShowGift() {
        if (giftBottomDialog == null) {
            giftBottomDialog = new GiftBottomDialog(this, identify);
            giftBottomDialog.setDoSendGiftListen(this);
        }
        giftBottomDialog.show();
    }

    //点击打赏金币
    private void clickReward() {
        new CuckooRewardCoinDialog(this).show();
    }

    //选择私照
    private void clickSelectPrivatePhoto() {

        Intent intent = new Intent(this, PrivatePhotoActivity.class);
        intent.putExtra(PrivatePhotoActivity.USER_ID, SaveData.getInstance().getId());
        intent.putExtra(PrivatePhotoActivity.USER_NAME, userName);
        intent.putExtra(PrivatePhotoActivity.ACTION_TYPE, 1);
        startActivityForResult(intent, RESULT_SELECT_PRIVATE_PHOTO);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventClickPrivateImg(EventChatClickPrivateImgMessage var1) {

        Common.requestSelectPic(this, var1.getId());
    }

    @Override
    public void onSuccess(JsonRequestDoPrivateSendGif sendGif) {

        CustomMsgPrivateGift gift = new CustomMsgPrivateGift();
        gift.fillData(sendGif.getSend());
        Message message = new CustomMessage(gift, LiveConstant.CustomMsgType.MSG_PRIVATE_GIFT);
        presenter.sendMessage(message.getMessage());
        //giftBottomDialog.dismiss();
    }


}
