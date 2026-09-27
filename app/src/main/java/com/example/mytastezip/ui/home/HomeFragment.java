package com.example.mytastezip.ui.home;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;

import com.example.mytastezip.ui.add.CategoryAdapter;
import com.example.mytastezip.ui.add.searchActivity;
import com.example.mytastezip.ui.savedList.Info;
import com.example.mytastezip.ui.savedList.InfoDao;
import com.example.mytastezip.ui.savedList.InfoViewModel;
import com.example.mytastezip.ui.savedList.RoomDB;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.kakao.vectormap.KakaoMap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import com.example.mytastezip.R;
import com.example.mytastezip.databinding.FragmentHomeBinding;
import com.example.mytastezip.ui.add.AddFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapType;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.MapViewInfo;
import com.kakao.vectormap.camera.CameraUpdate;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;

import java.text.BreakIterator;
import java.util.List;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private MapView mapView;
    private FloatingActionButton fab;
    private ImageButton btnZoomIn, btnZoomOut, currentLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private KakaoMap kakaoMap;
    private InfoDao infoDao;
    private EditText etLocation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 권한 요청 런처 초기화
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        getCurrentLocationAndMove(kakaoMap);
                    } else {
                        Toast.makeText(getContext(), "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        RoomDB db = Room.databaseBuilder(
                requireContext().getApplicationContext(),
                RoomDB.class,
                "my-taste-zip-db"
        ).allowMainThreadQueries().build();

        // 1. MapView 생성
        mapView = view.findViewById(R.id.map_view);

        // FAB 버튼 클릭 리스너
        fab = binding.fab;
        fab.setOnClickListener(v -> showAddFragment());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        btnZoomIn = view.findViewById(R.id.btnZoomIn);
        btnZoomOut = view.findViewById(R.id.btnZoomOut);
        currentLocation = view.findViewById(R.id.currentLocation);

        initDatabase();


        if (mapView != null) {
            mapView.start(new MapLifeCycleCallback() {
                @Override
                public void onMapDestroy() {
                    Log.d("DEBUG", "onMapDestroy");
                }

                @Override
                public void onMapError(Exception error) {
                    Log.e("KAKAO_MAP", "지도 오류 발생: " + error.getMessage(), error);
                    Toast.makeText(requireContext(), "지도 초기화 오류: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }, new KakaoMapReadyCallback() {

                @Override
                public void onMapReady(KakaoMap map) {
                    Log.d("DEBUG", "onMapReady 호출됨 - map: " + (map != null ? "not null" : "null"));

                    kakaoMap = map; // KakaoMap 객체 할당
                    Log.d("DEBUG", "할당 후 kakaoMap 상태: " + (kakaoMap != null ? "not null" : "null"));
                    setupButtonListenersInCallback(map);

                }

                @Override
                public LatLng getPosition() {
                    // 지도 시작 시 위치 좌표를 설정
                    // 한국의 중심 좌표 (대략 대전 근처)
                    Log.d("DEBUG", "getPosition 호출됨 - 한국 중심");
                    return LatLng.from(36.3504, 127.8294);
                }

                @Override
                public int getZoomLevel() {
                    // 지도 시작 시 확대/축소 줌 레벨 설정
                    // 한국 전체가 보이는 줌 레벨 (6-8 정도)
                    Log.d("DEBUG", "getZoomLevel 호출됨 - 한국 전체 보기");
                    return 7;
                }

                @Override
                public MapViewInfo getMapViewInfo() {
                    // 지도 시작 시 App 및 MapType 설정
                     return MapViewInfo.from(
                            "openmap",
                            MapType.NORMAL
                    );
                }

            });

        } else {
            Log.e("KAKAO_MAP", "MapView가 XML에서 로드되지 않았습니다. ID를 확인하세요.");
            Toast.makeText(requireContext(), "지도 로드 실패: 레이아웃 확인 필요", Toast.LENGTH_LONG).show();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.resume();
        }
    }

    private void setupButtonListenersInCallback(KakaoMap map) {
        Log.d("DEBUG", "setupButtonListenersInCallback - map: " + (map != null ? "OK" : "NULL"));

        if (map == null) {
            Log.e("DEBUG", "전달받은 map이 null입니다!");
            observeInfoData();
            return;
        }

        currentLocation.setOnClickListener(v -> {
            if (checkLocationPermission()) {
                Log.d("Location","setCurrentLocation");
                getCurrentLocationAndMove(map);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        // 멤버 변수 대신 파라미터로 받은 map 사용
        btnZoomIn.setOnClickListener(v -> {
            int currentLevel = map.getZoomLevel();
            if (currentLevel < 21) {
                map.moveCamera(CameraUpdateFactory.zoomTo(currentLevel + 1));
            }
        });

        btnZoomOut.setOnClickListener(v -> {
            int currentLevel = map.getZoomLevel();
            if (currentLevel > 1) {
                map.moveCamera(CameraUpdateFactory.zoomTo(currentLevel - 1));
            }
        });

        // 멤버 변수에도 할당
        kakaoMap = map;
        Log.d("DEBUG", "최종 kakaoMap 할당 완료: " + (kakaoMap != null ? "OK" : "NULL"));

        // 핀 클릭시, 정보 레이아웃 표시
        map.setOnLabelClickListener((kakaoMap, layer, label) -> {
            String labelId = label.getLabelId();
            int infoId = Integer.parseInt(labelId);
            showInfoBottomSheet(infoId);
            return true;
        });

        // LiveData 관찰 시작
        observeInfoData();

        loadSavedLocationsAndAddMarkers();
    }

    private void showInfoBottomSheet(int infoId) {
        new Thread(() -> {
            Info info = infoDao.getInfoById(infoId);

            requireActivity().runOnUiThread(() -> {
                if (info != null) {
                    createBottomSheet(info);
                }
            });
        }).start();
    }

    private void createBottomSheet(Info info) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());

        View bottomSheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_info, null);

        Button btnEdit = bottomSheetView.findViewById(R.id.btn_edit);
        Button btnDelete = bottomSheetView.findViewById(R.id.btn_delete);

        btnEdit.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showEditDialog(info); // info 객체로 수정 다이얼로그 띄우기
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("삭제 확인")
                    .setMessage("정말 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        new Thread(() -> {
                            infoDao.delete(info); // RoomDB에서 삭제
                        }).start();
                        bottomSheetDialog.dismiss();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        // 나머지 바인딩 및 다이얼로그 표시 코드
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

        ImageView ivCategory = bottomSheetView.findViewById(R.id.iv_category);
        TextView tvName = bottomSheetView.findViewById(R.id.tv_name);
        TextView tvLocation = bottomSheetView.findViewById(R.id.tv_location);
        TextView tvCategory = bottomSheetView.findViewById(R.id.tv_category);
        TextView tvMemo = bottomSheetView.findViewById(R.id.tv_memo);
        TextView tvLink = bottomSheetView.findViewById(R.id.tv_link);

        setCategoryIcon(ivCategory, info.getCategory());

        tvName.setText(info.getName());
        tvLocation.setText(info.getLocation());
        tvCategory.setText(info.getCategory());
        tvMemo.setText(info.getMemo());
        tvLink.setText(info.getLink());

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void showEditDialog(Info info) {
        View root = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_info, null);

        EditText etName = root.findViewById(R.id.et_name);
        etLocation = root.findViewById(R.id.et_location);
        MaterialAutoCompleteTextView autoCompleteCategory = root.findViewById(R.id.autoCompleteCategory);
        EditText etMemo = root.findViewById(R.id.et_memo);
        EditText etLink = root.findViewById(R.id.et_link);
        Button btnSave = root.findViewById(R.id.btnSave);
        Button btnCancel = root.findViewById(R.id.btnCancel);


        // 기존 값 세팅
        etName.setText(info.getName());
        etLocation.setText(info.getLocation());
        autoCompleteCategory.setText(info.getCategory());
        etMemo.setText(info.getMemo());
        etLink.setText(info.getLink());

        // 카테고리 자동완성
        String[] categories = {"한식", "중식", "일식", "양식", "카페"};
        CategoryAdapter adapter = new CategoryAdapter(requireContext(), categories);
        autoCompleteCategory.setAdapter(adapter);

        // 주소 입력 EditText 비활성화 & 클릭시 주소 검색
        etLocation.setFocusable(false);
        etLocation.setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), searchActivity.class);
            getSearchResult.launch(intent);
        });

        // AlertDialog에 커스텀 뷰 세팅
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(root);
        AlertDialog dialog = builder.create();

        // 저장 버튼 클릭
        btnSave.setOnClickListener(v -> {
            info.setName(etName.getText().toString());
            info.setLink(etLink.getText().toString());
            info.setMemo(etMemo.getText().toString());
            info.setLocation(etLocation.getText().toString());
            info.setCategory(autoCompleteCategory.getText().toString());

            new Thread(() -> infoDao.update(info)).start();
            dialog.dismiss();
        });

        // 취소 버튼 클릭
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.setOnDismissListener(dialogInterface -> {
            etLocation = null;  // 다이얼로그 닫힐 때 null로 초기화
        });
        dialog.show();

    }

    private final ActivityResultLauncher<Intent> getSearchResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //searchActivity 데이터가 여기로 전달됨
                if (result.getResultCode() == RESULT_OK ) {
                    if (result.getData() != null){
                        String data = result.getData().getStringExtra("data");
                        etLocation.setText(data);
                    }
                }
            }
    );

    private void setCategoryIcon(ImageView ivCategory, String category) {
        if (category.equals("한식")) {
            ivCategory.setImageResource(R.drawable.rice_pin);
        } else if (category.equals("중식")) {
            ivCategory.setImageResource(R.drawable.dimsum_pin);
        } else if (category.equals("일식")) {
            ivCategory.setImageResource(R.drawable.sushi_pin);
        } else if (category.equals("양식")) {
            ivCategory.setImageResource(R.drawable.pasta_pin);
        } else if (category.equals("카페")) {
            ivCategory.setImageResource(R.drawable.coffee_pin);
        }
    }


    private void observeInfoData() {
        if (infoDao != null) {
            infoDao.getAllInfos().observe(getViewLifecycleOwner(), newInfos -> {
                Log.d("DEBUG", "LiveData 변화 감지: " + (newInfos != null ? newInfos.size() : "null") + "개");

                if (newInfos != null && kakaoMap != null) {
                    // 새로운 데이터로 마커 업데이트
                    addMarkersToMap(newInfos);
                }
            });
        }
    }

    // Room DB 초기화
    private void initDatabase() {
        RoomDB database = Room.databaseBuilder(requireContext(),
                        RoomDB.class, "my-taste-zip-db")
                .build();
        infoDao = database.infoDao();
    }

    // 저장된 위치들을 지도에 마커로 표시
    private void loadSavedLocationsAndAddMarkers() {
        new Thread(() -> {
            try {
                Log.d("DEBUG", "DB 조회 시작");
                List<Info> savedInfos = infoDao.getAllInfosSync();
                Log.d("DEBUG", "DB 조회 완료: " + (savedInfos != null ? savedInfos.size() : "null") + "개");

                if (savedInfos != null) {
                    for (Info info : savedInfos) {
                        Log.d("DEBUG", "조회된 데이터: " + info.getName() + " (" +
                                info.getLatitude() + ", " + info.getLongitude() + ")");
                    }
                }

                requireActivity().runOnUiThread(() -> {
                    addMarkersToMap(savedInfos);
                });
            } catch (Exception e) {
                Log.e("DEBUG", "DB 조회 오류: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    // 마커들을 지도에 추가
    private void addMarkersToMap(List<Info> infos) {
        if (kakaoMap == null || infos == null || infos.isEmpty()) {
            Log.d("DEBUG", "마커 추가 불가: kakaoMap=" + (kakaoMap != null) +
                    ", infos=" + (infos != null ? infos.size() : "null"));
            return;
        }

        // 기존 마커 제거
        kakaoMap.getLabelManager().getLayer().removeAll();

        // 첫 번째 마커 위치로 지도 이동
        if (!infos.isEmpty()) {
            Info firstInfo = infos.get(0);
            if (firstInfo.getLatitude() != 0.0 && firstInfo.getLongitude() != 0.0) {
                LatLng firstPosition = LatLng.from(firstInfo.getLatitude(), firstInfo.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCenterPosition(firstPosition, 15);
                kakaoMap.moveCamera(cameraUpdate);
                Log.d("DEBUG", "지도 이동: " + firstInfo.getName() + " 위치로 (" +
                        firstInfo.getLatitude() + ", " + firstInfo.getLongitude() + ")");
            }
        }

        // 모든 마커 추가
        for (Info info : infos) {
            if (info.getLatitude() != 0.0 && info.getLongitude() != 0.0) {
                addSingleMarker(info);
            }
        }

        Log.d("DEBUG", "총 " + infos.size() + "개의 마커가 추가되었습니다.");
    }

    // 개별 마커 추가
    private void addSingleMarker(Info info) {
        try {
            LatLng position = LatLng.from(info.getLatitude(), info.getLongitude());

            // 카테고리별로 핀 이미지 선택
            int pinResId = getPinResourceByCategory(info.getCategory());

            // PNG 이미지를 Bitmap으로 변환
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), pinResId);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 90, 100, false);

            // Bitmap을 drawable 리소스로 등록
            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), resizedBitmap);

            LabelOptions labelOptions = LabelOptions.from(String.valueOf(info.getId()), position)
                    .setStyles(LabelStyle.from(bitmapDrawable.getBitmap())
                            .setAnchorPoint(0.5f, 1.0f));


            kakaoMap.getLabelManager().getLayer().addLabel(labelOptions);

            Log.d("DEBUG", "마커 추가: " + info.getName() + " (" + info.getLatitude() + ", " + info.getLongitude() + ")");

        } catch (Exception e) {
            Log.e("DEBUG", "마커 추가 실패: " + e.getMessage());
        }
    }

    private int getPinResourceByCategory(String category) {
        if (category.equals("한식")) {
            return R.drawable.rice_pin;
        } else if (category.equals("중식")) {
            return R.drawable.dimsum_pin;
        } else if (category.equals("양식")) {
            return R.drawable.pasta_pin;
        } else if (category.equals("일식")) {
            return R.drawable.sushi_pin;
        } else if (category.equals("카페")) {
            return R.drawable.coffee_pin;
        } else {
            return R.drawable.rice_pin; // 기본값
        }
    }


    @SuppressLint("MissingPermission")
    private void getCurrentLocationAndMove(KakaoMap map) {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // 실제 현재 위치 좌표
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        Log.d("DEBUG", "현재 위치: " + latitude + ", " + longitude);

                        // 한국 좌표 범위 확인 (대략적)
                        if (latitude >= 33.0 && latitude <= 38.6 &&
                                longitude >= 124.0 && longitude <= 132.0) {

                            LatLng currentLatLng = LatLng.from(latitude, longitude);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newCenterPosition(currentLatLng, 15);
                            map.moveCamera(cameraUpdate);
                            Toast.makeText(getContext(), "현재 위치로 이동", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "한국 좌표가 아닙니다: " + latitude + ", " + longitude, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        requestCurrentLocation(map);
                    }
                });
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void requestCurrentLocation(KakaoMap map) {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdates(1)
                .build();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLastLocation();
                    LatLng currentLatLng = LatLng.from(location.getLatitude(), location.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCenterPosition(currentLatLng);
                    map.moveCamera(cameraUpdate);

                    Toast.makeText(getContext(), "현재 위치로 이동", Toast.LENGTH_SHORT).show();
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.pause();    // MapView 의 pause 호출
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    private void showAddFragment() {
        AddFragment fragmentAdd = new AddFragment();
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragmentAdd)
                .addToBackStack(null)
                .commit();
    }


}