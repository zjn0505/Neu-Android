package xyz.jienan.pushpull.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import xyz.jienan.pushpull.R;
import xyz.jienan.pushpull.network.MemoEntity;

/**
 * Created by Jienan on 2017/11/1.
 */

public class PushPullAdapter extends RecyclerView.Adapter<PushPullAdapter.ViewHolder> implements TouchHelperAdapter {

    private final static String MEMO_KEY = "memo_key";
    private ClipboardManager clipboard;
    private List<MemoEntity> mList;
    private Context mContext;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private RecyclerView recyclerView;

    PushPullAdapter(Context context) {
        mList = new LinkedList<MemoEntity>();
        mContext = context;
        if (clipboard == null) {
            clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        }
        gson = new Gson();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("Memo", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(MEMO_KEY, null);
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String item = a.optString(i);
                    MemoEntity entity = gson.fromJson(item, MemoEntity.class);
                    if (entity != null)
                        mList.add(entity);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        editor = sharedPreferences.edit();
    }

    public void onItemMove(int from, int to) {
        Collections.swap(mList, from, to);
        notifyItemMoved(from, to);
        saveToPreference();
    }

    @Override
    public void onItemDismiss(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
        saveToPreference();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView tvId;
        public final TextView tvMsg;
        public final ImageView ivCopy;

        public ViewHolder(View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_push_short_id);
            tvMsg = itemView.findViewById(R.id.tv_push_content);
            ivCopy = itemView.findViewById(R.id.iv_copy);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final MemoEntity memo = mList.get(position);
        holder.tvId.setText(memo.getId());
        holder.tvMsg.setText(memo.getMsg());
        holder.ivCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clip = ClipData.newPlainText("id",memo.getId());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mContext, "id copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addMemo(MemoEntity memo) {
        if (mList.contains(memo)) {
            int index = mList.indexOf(memo);
            Collections.swap(mList, index, 0);
            this.notifyItemMoved(index, 0);
            Toast.makeText(mContext, "item already in list", Toast.LENGTH_SHORT).show();
        } else {
            mList.add(0, memo);
            notifyItemInserted(0);
        }
        recyclerView.scrollToPosition(0);
        saveToPreference();
    }

    private void saveToPreference() {
        JSONArray a = new JSONArray();
        for (MemoEntity entity : mList) {
            a.put(gson.toJson(entity));
        }

        editor.putString(MEMO_KEY, a.toString());
        editor.commit();
    }
}
