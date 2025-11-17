package com.example.sportconnection.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.sportconnection.fragments.PostsListFragment;

public class PostsPagerAdapter extends FragmentStateAdapter {
    private PostsListFragment allPostsFragment;
    private PostsListFragment myPostsFragment;

    public PostsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public PostsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            // Todos los posts
            allPostsFragment = PostsListFragment.newInstance(false);
            return allPostsFragment;
        } else {
            // Mis posts
            myPostsFragment = PostsListFragment.newInstance(true);
            return myPostsFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Dos pestañas
    }

    public PostsListFragment getAllPostsFragment() {
        return allPostsFragment;
    }

    public PostsListFragment getMyPostsFragment() {
        return myPostsFragment;
    }
}

