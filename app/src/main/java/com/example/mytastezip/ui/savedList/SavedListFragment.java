package com.example.mytastezip.ui.savedList;

import android.app.AlertDialog; // 알림 대화상자용
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.mytastezip.R;
import com.example.mytastezip.databinding.FragmentSavedListBinding;

import java.util.List;

public class SavedListFragment extends Fragment implements InfoListAdapter.OnItemClickListener {

    private FragmentSavedListBinding binding;
    private RoomDB database;
    private InfoDao infoDao;
    private RecyclerView recyclerView;
    private InfoListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSavedListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        database = Room.databaseBuilder(requireContext().getApplicationContext(),
                        RoomDB.class, "my-taste-zip-db")
                .allowMainThreadQueries()
                .build();
        infoDao = database.infoDao();

        recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InfoListAdapter();
        recyclerView.setAdapter(adapter);

        // 어댑터에 클릭 리스너 설정 (SavedListFragment 자신이 리스너 역할을 함)
        adapter.setOnItemClickListener(this);

        loadInfoList(); // 데이터 로드

        return root;
    }

    private void loadInfoList() {
        List<Info> infoList = infoDao.getAll();

        if (infoList != null && !infoList.isEmpty()) {
            adapter.setInfoList(infoList);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            adapter.setInfoList(infoList); // 목록이 비었음을 어댑터에 알림
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadInfoList(); // Fragment가 다시 활성화될 때 목록 새로고침
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(Info info) {
        // 아이템 클릭 시 자세한 내용 창 띄우기
        showDetailDialog(info);
    }

    @Override
    public void onDeleteClick(Info info) {
        // 삭제 버튼 클릭 시 확인 대화상자 띄우기
        new AlertDialog.Builder(requireContext())
                .setTitle("맛집 삭제")
                .setMessage("'" + info.name + "' 맛집 정보를 정말 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    // Room DB에서 삭제
                    try {
                        infoDao.delete(info);
                        adapter.removeItem(info); // 어댑터에서 아이템 제거
                        Toast.makeText(getContext(), "'" + info.name + "'이(가) 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        loadInfoList(); // 삭제 후 목록 새로고침하여 "데이터 없음" 처리
                        Log.d("Delete", "onDeleteClick: "+info.name+"맛집이 삭제됨");
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "삭제 중 오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // 상세 정보 대화상자를 보여주는 메서드
    private void showDetailDialog(Info info) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(info.name + " 상세 정보");

        // 전체 메시지 빌드
        StringBuilder sb = new StringBuilder();
        sb.append("이름: ").append(info.name).append("\n");
        sb.append("장소: ").append(info.location).append("\n");
        sb.append("카테고리: ").append(info.category).append("\n");
        if (info.link != null && !info.link.isEmpty()) {
            sb.append("링크: ").append(info.link).append("\n");
        }
        if (info.memo != null && !info.memo.isEmpty()) {
            sb.append("메모: ").append(info.memo).append("\n");
        }

        SpannableString spannable = new SpannableString(sb.toString());

        // 링크에만 ClickableSpan 적용
        if (info.link != null && !info.link.isEmpty()) {
            int start = sb.indexOf(info.link);
            int end = start + info.link.length();

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(info.link));
                    widget.getContext().startActivity(intent);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.parseColor("#1DCD9F")); // 링크 색상
                    ds.setUnderlineText(true); // 밑줄 표시 여부
                }
            };

            spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // TextView에 Spannable 적용
        TextView messageView = new TextView(requireContext());
        messageView.setText(spannable);
        messageView.setMovementMethod(LinkMovementMethod.getInstance());
        messageView.setPadding(50, 40, 50, 10);

        builder.setView(messageView);
        builder.setPositiveButton("확인", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

}