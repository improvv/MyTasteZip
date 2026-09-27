package com.example.mytastezip.ui.savedList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout; // LinearLayout을 사용했으니 import
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytastezip.R;

import java.util.ArrayList;
import java.util.List;

public class InfoListAdapter extends RecyclerView.Adapter<InfoListAdapter.InfoViewHolder> {

    private List<Info> infoList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Info info);
        void onDeleteClick(Info info);
    }

    // 리스너 설정 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setInfoList(List<Info> infoList) {
        this.infoList = infoList;
        notifyDataSetChanged();
    }

    // 특정 아이템 삭제 후 목록 업데이트
    public void removeItem(Info info) {
        int position = infoList.indexOf(info);
        if (position != -1) {
            infoList.remove(position);
            notifyItemRemoved(position);
        }
    }


    @NonNull
    @Override
    public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_list, parent, false);
        return new InfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoViewHolder holder, int position) {
        Info currentInfo = infoList.get(position);
        holder.bind(currentInfo, listener); // 리스너를 bind 메서드로 전달
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    // ViewHolder 내부 클래스
    static class InfoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvLocation;
        private final ImageView btnDelete;
        private final LinearLayout itemContainer; // 전체 아이템 컨테이너

        public InfoViewHolder(@NonNull View itemView) {
            super(itemView);
            itemContainer = itemView.findViewById(R.id.item_container); // ID 연결
            tvName = itemView.findViewById(R.id.nameInfo); // ID 변경 (nameInfo)
            tvLocation = itemView.findViewById(R.id.locInfo); // ID 변경 (locInfo)
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Info info, OnItemClickListener listener) {
            tvName.setText(info.name);
            tvLocation.setText(info.location);

            // 전체 아이템 클릭 이벤트
            itemContainer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(info);
                }
            });

            // 삭제 버튼 클릭 이벤트
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(info);
                }
            });
        }
    }
}