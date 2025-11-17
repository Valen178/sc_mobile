package com.example.sportconnection.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sportconnection.CreatePostActivity;
import com.example.sportconnection.R;
import com.example.sportconnection.adapters.PostsPagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class PostsFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabCreatePost;
    private PostsPagerAdapter pagerAdapter;

    private ActivityResultLauncher<Intent> createPostLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Registrar el launcher para crear post
        createPostLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Recargar los posts cuando se crea uno nuevo
                        refreshPosts();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        fabCreatePost = view.findViewById(R.id.fabCreatePost);

        setupViewPager();
        setupFab();
    }

    private void setupViewPager() {
        pagerAdapter = new PostsPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Conectar TabLayout con ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Todas las publicaciones");
                    } else {
                        tab.setText("Mis publicaciones");
                    }
                }
        ).attach();
    }

    private void setupFab() {
        fabCreatePost.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreatePostActivity.class);
            createPostLauncher.launch(intent);
        });
    }

    private void refreshPosts() {
        // Obtener los fragmentos y refrescarlos
        if (pagerAdapter.getAllPostsFragment() != null) {
            pagerAdapter.getAllPostsFragment().refreshPosts();
        }
        if (pagerAdapter.getMyPostsFragment() != null) {
            pagerAdapter.getMyPostsFragment().refreshPosts();
        }
    }
}
