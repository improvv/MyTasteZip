package com.example.mytastezip.ui.add;

import static android.app.Activity.RESULT_OK;
import android.content.Intent;
import com.example.mytastezip.BuildConfig;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;

import com.example.mytastezip.R;
import com.example.mytastezip.databinding.FragmentAddBinding;
import com.example.mytastezip.ui.savedList.Info;
import com.example.mytastezip.ui.savedList.InfoDao;
import com.example.mytastezip.ui.savedList.RoomDB;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.Gson;


public class AddFragment extends Fragment {

    private FragmentAddBinding binding;
    private EditText etname, etlocation, etmemo, etlink;
    private Button btnSave, btnCancle;
    private MaterialAutoCompleteTextView autoCompleteCategory;
    private RoomDB database;
    private InfoDao infoDao;
    private static final String KAKAO_REST_API_KEY = BuildConfig.KAKAO_REST_API_KEY; // local.properties에서 주입
    private AddViewModel addViewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // AddViewModel 초기화 (Activity 범위로 공유)
        addViewModel = new ViewModelProvider(requireActivity()).get(AddViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAddBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        database = Room.databaseBuilder(requireContext().getApplicationContext(),
                        RoomDB.class, "my-taste-zip-db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        infoDao = database.infoDao();

        String[] categories = {"한식", "중식", "일식", "양식", "카페"};
        AutoCompleteTextView autoCompleteTextView = root.findViewById(R.id.autoCompleteCategory);

        CategoryAdapter adapter = new CategoryAdapter(requireContext(), categories);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteCategory = root.findViewById(R.id.autoCompleteCategory);
        etname = root.findViewById(R.id.etname);
        etlink = root.findViewById(R.id.etlink);
        etmemo = root.findViewById(R.id.etmemo);
        etlocation = root.findViewById(R.id.etlocation);

        // MainActivity에서 전달된 링크 가져오기
        if (getArguments() != null) {
            String sharedLink = getArguments().getString("shared_link");
            if (sharedLink != null) {
                etlink.setText(sharedLink);
            }
        }

        etlocation.setFocusable(false);
        etlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //주소 검색 웹뷰 화면 이동
                Intent intent = new Intent(requireContext(), searchActivity.class);
                getSearchResult.launch(intent);
            }
        });


        btnSave = root.findViewById(R.id.btnSave);
        btnCancle = root.findViewById(R.id.btnCancle);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // EditText/AutoCompleteTextView에서 사용자 입력 값 가져오기
                String category = autoCompleteCategory.getText().toString().trim();
                String name = etname.getText().toString().trim();
                String location = etlocation.getText().toString().trim();
                String link = etlink.getText().toString().trim();
                String memo = etmemo.getText().toString().trim();

                //필수 입력 값
                if (location.isEmpty() || name.isEmpty() || category.isEmpty()) {
                    Toast.makeText(getContext(), "장소, 이름, 카테고리는 필수 입력 항목입니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 주소를 위경도로 변환 후 저장 로직 실행
                Log.d("DEBUG", "geocodeAndSave 호출: " + location);
                geocodeAndSave(location, category, name, link, memo);
            }
        });

        btnCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    // requireActivity().finish();
                    Toast.makeText(getContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                    autoCompleteCategory.setText("");
                    etname.setText("");
                    etlocation.setText("");
                    etlink.setText("");
                    etmemo.setText("");
                }
            }
        });

        return root;
    }

    private final ActivityResultLauncher<Intent> getSearchResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //searchActivity 데이터가 여기로 전달됨
                if (result.getResultCode() == RESULT_OK ) {
                    if (result.getData() != null){
                        String data = result.getData().getStringExtra("data");
                        etlocation.setText(data);
                    }
                }
            }
    );

    // 주소를 위경도로 변환하고 DB에 저장, HomeFragment로 핀 정보 전달
    private void geocodeAndSave(String address, String category, String name, String link, String memo) {
        Log.d("DEBUG", "geocodeAndSave 시작: " + address);
        String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + address;
        Request request = new Request.Builder()
                .header("Authorization", "KakaoAK " + KAKAO_REST_API_KEY)
                .url(url)
                .build();

        OkHttpClient httpClient = new OkHttpClient();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "주소 검색 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    Gson gson = new Gson();
                    AddressSearchResult searchResult = gson.fromJson(responseData, AddressSearchResult.class);

                    if (searchResult != null && searchResult.documents != null && searchResult.documents.length > 0) {
                        Document doc = searchResult.documents[0];
                        double longitude = Double.parseDouble(doc.x);
                        double latitude = Double.parseDouble(doc.y);

                        // DB 저장 및 HomeFragment로 핀 정보 전달 (메인 스레드에서 실행)
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                // Room DB에 정보 저장
                                Info newInfo = new Info();
                                newInfo.location = address;
                                newInfo.category = category;
                                newInfo.name = name;
                                newInfo.link = link;
                                newInfo.memo = memo;
                                newInfo.latitude = latitude; // 위도 저장
                                newInfo.longitude = longitude; // 경도 저장

                                try {
                                    infoDao.insert(newInfo);
                                    Toast.makeText(getContext(), "맛집 정보가 저장되었습니다!", Toast.LENGTH_SHORT).show();

                                    // HomeFragment로 핀 데이터 전달
                                    // AddViewModel을 통해 전달. HomeFragment에서 이를 관찰하여 지도에 핀 추가.
                                    addViewModel.addPinData(new PinData(latitude, longitude, name, category, newInfo.id)); // ID도 같이 전달

                                    clearInputFields(); // 입력 필드 초기화

                                    // 핀 추가 후 HomeFragment로 이동
                                    NavController navController = Navigation.findNavController(btnSave);
                                    navController.navigate(R.id.nav_home); // 네비게이션 ID 확인
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), "저장 중 오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            });
                        }
                    } else {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show());
                        }
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "주소 검색 응답 실패: " + response.code(), Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void clearInputFields() {
        autoCompleteCategory.setText("", false); // AutoCompleteTextView는 setText(text, filter) 사용
        etname.setText("");
        etlocation.setText("");
        etlink.setText("");
        etmemo.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // 카카오 로컬 검색 API 응답을 위한 데이터 클래스
    private static class AddressSearchResult {
        @SerializedName("documents")
        public Document[] documents;
    }

    private static class Document {
        @SerializedName("x") // 경도
        public String x;
        @SerializedName("y") // 위도
        public String y;
        @SerializedName("address_name")
        public String addressName;
    }

    // 핀 데이터를 담을 클래스 (ViewModel을 통해 전달)
    public static class PinData {
        public double latitude;
        public double longitude;
        public String title; // 맛집 이름
        public String category;
        public int dbId; // DB에 저장된 Info의 ID (핀 클릭 시 DB 정보 가져올 때 유용)

        public PinData(double latitude, double longitude, String title, String category, int dbId) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.title = title;
            this.category = category;
            this.dbId = dbId;
        }
    }
}