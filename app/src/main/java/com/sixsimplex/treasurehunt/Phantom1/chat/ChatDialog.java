package com.sixsimplex.treasurehunt.Phantom1.chat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.Phantom1.chat.websocket.WebSocketClass;
import com.sixsimplex.treasurehunt.Phantom1.chat.websocket.WebSocketEventListener;
import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Response;
import okio.ByteString;

public class ChatDialog extends BottomSheetDialogFragment implements WebSocketEventListener {
    WebSocketClass webSocketClass;
    private LinearLayout chat_msg_container;
    private EditText chat_input_textview;
    private TextView headlineTv;
    private ImageButton send_message_button;
    private ImageView refresh_button;
    CMEntity currentEntity;
    private Activity parentActivity;
    private Feature feature;
    private JSONObject currentVertex;

    List<ChatMessageViewModel> chatMessageViewModelList=new ArrayList<>();
    public static ChatDialog newInstance(Activity activity, CMEntity cmEntity,
            Feature feature,JSONObject currentVertex) {
        ChatDialog infoFragmentDialog = new ChatDialog();
        infoFragmentDialog.setParentActivity(activity);
        infoFragmentDialog.setCurrentEntity(cmEntity);
        infoFragmentDialog.setFeature(feature);
        infoFragmentDialog.setCurrentVertex(currentVertex);
        return infoFragmentDialog;
    }

    public void setCurrentVertex(JSONObject currentVertex) {
        this.currentVertex = currentVertex;
    }



    public void setCurrentEntity(CMEntity currentEntity) {
        this.currentEntity = currentEntity;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public void setParentActivity(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dialog_chat, container, false);
        ButterKnife.bind(this, view);
        view.setBackgroundColor(Color.WHITE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    private void init(View view) {
        chat_msg_container = view.findViewById(R.id.chat_msg_container);
        chat_input_textview = view.findViewById(R.id.chat_input_textview);
        send_message_button = view.findViewById(R.id.send_message_button);
        refresh_button=view.findViewById(R.id.refreshIV);
        headlineTv = view.findViewById(R.id.headlineTv);
        headlineTv.setText("Chat with admin");
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initCheckWebSocket();


        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        send_message_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputManager != null) {
                        if (v != null) {
                            inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(chat_input_textview.getText()==null ||chat_input_textview.getText().toString().isEmpty()){
                    Toast.makeText(getContext(),"Please enter a message",Toast.LENGTH_SHORT).show();
                }else {

                    /*
                    * {
    "time": "10-01-2022 17:09:25",
    "type": "data or notification",
	"text": "testing messaging with Eesha",
    "toUserName": "dipeshajmera"
}
                    * */
                    try{
                        JSONObject messageJson = new JSONObject();
                        String time=SystemUtils.getCurrentDateTime();
                        messageJson.put("time", time);
                        messageJson.put("type","data");
                        messageJson.put("text",chat_input_textview.getText().toString());
                        messageJson.put("toUserName","teamd");

                        webSocketClass.sendMessage(messageJson.toString(),getContext());
                        chat_input_textview.setText("");
                        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_dialog_chat_message_sent, chat_msg_container, false);
                        TextView tv_message = view.findViewById(R.id.chat_textview);
                        tv_message.setText(chat_input_textview.getText().toString());
                        tv_message.setBackgroundColor(requireContext().getResources().getColor(R.color.color_blu1e));
                        TextView tv_time = view.findViewById(R.id.time_textview);
                        tv_time.setText(time);
                        chat_msg_container.addView(view);
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                }
            }
        });
    }

    private void initCheckWebSocket() {
        if (webSocketClass == null) {
            webSocketClass = new WebSocketClass(UrlStore.getMessageSocketUrl(), "init",this);
        }
    }

    public WebSocketClass getWebSocketManager() {
        return webSocketClass;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog bottomSheetDialog1 = (BottomSheetDialog) dialog;
            View bottomSheet = bottomSheetDialog1.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setPeekHeight(Resources.getSystem().getDisplayMetrics().heightPixels / 3);
                BottomSheetBehavior.from(bottomSheet).setFitToContents(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(false);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
            }
        });

        return bottomSheetDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @OnClick(R.id.cancelIV)
    public void onClickEvent(View view) {
        if (view.getId() == R.id.cancelIV) {
            dismiss();
        }else if(view.getId() == R.id.refreshIV){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                refreshMessages();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void refreshMessages() {


        chatMessageViewModelList.sort(ChatMessageViewModel.getComparator());

    }

    @Override
    public void onSocketOpened(Response response) {
        send_message_button.setEnabled(true);
    }

    @Override
    public void onMessageReceived(String receivedMessage) {
        if(receivedMessage==null||receivedMessage.isEmpty()){
            return;
        }
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_dialog_chat_message_received, chat_msg_container, false);
        TextView tv_message = view.findViewById(R.id.chat_textview);
        tv_message.setText(receivedMessage);
        chat_msg_container.addView(view);
    }

    @Override
    public void onMessageReceived(ByteString receivedBytes) {

    }
}
