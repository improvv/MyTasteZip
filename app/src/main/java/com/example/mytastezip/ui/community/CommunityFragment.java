package com.example.mytastezip.ui.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytastezip.R;
import com.example.mytastezip.ui.savedList.RoomDB;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {
    private RoomDB db;
    private PostDao postDao;
    private RecyclerView recyclerView;
    private PostAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community, container, false);

        db = RoomDB.getInstance(requireContext());
        postDao = db.postDao();

        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnEditClickListener(post -> showEditDialog(post));

        // 롱클릭으로 게시글 삭제
        adapter.setOnItemLongClickListener((position, post) -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("게시글 삭제")
                    .setMessage("이 게시글을 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        // 실제 DB에서도 삭제
                        List<Post> currentList = new ArrayList<>(adapter.getPostList());
                        currentList.remove(position);
                        adapter.submitList(currentList); // 새 리스트로 갱신
                        new Thread(() -> postDao.delete(post)).start();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        // LiveData로 게시글 목록 관찰
        postDao.getAllPosts().observe(getViewLifecycleOwner(), posts -> adapter.submitList(posts));

        // 글쓰기 버튼
        FloatingActionButton fab = root.findViewById(R.id.fab_write);
        fab.setOnClickListener(v -> showWriteDialog());

        return root;
    }

    private void showEditDialog(Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_write_post, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etContent = dialogView.findViewById(R.id.etContent);

        etTitle.setText(post.title);
        etContent.setText(post.content);

        builder.setView(dialogView)
                .setTitle("글 수정")
                .setPositiveButton("수정", (dialog, which) -> {
                    post.title = etTitle.getText().toString();
                    post.content = etContent.getText().toString();
                    // timestamp를 갱신하고 싶다면 아래 코드 추가
                    // post.timestamp = System.currentTimeMillis();
                    new Thread(() -> postDao.update(post)).start();
                })
                .setNegativeButton("취소", null)
                .show();
    }


    private void showWriteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_write_post, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etContent = dialogView.findViewById(R.id.etContent);

        builder.setView(dialogView)
                .setTitle("글쓰기")
                .setPositiveButton("등록", (dialog, which) -> {
                    String title = etTitle.getText().toString();
                    String content = etContent.getText().toString();
                    Post post = new Post();
                    post.title = title;
                    post.content = content;
                    post.author = "익명";
                    post.timestamp = System.currentTimeMillis();
                    new Thread(() -> postDao.insert(post)).start();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
