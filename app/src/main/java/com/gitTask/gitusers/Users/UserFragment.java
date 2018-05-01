package com.gitTask.gitusers.Users;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gitTask.gitusers.R;
import com.gitTask.gitusers.data.GitUser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Saurabh on 28/04/2018.
 */

public class UserFragment extends Fragment implements UserContract.View {

    private UserContract.Presenter mPresenter;
    private UserAdapter mAdapter;
    private String searchTerm = "";

    @BindView(R.id.users_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.search_edit_text)
    EditText searchEditText;

    public UserFragment() {
        // required empty constructor
    }

    public static UserFragment newInstance() {
        return new UserFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new UserAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    //Todo: add null check
    @Override
    public void setPresenter(UserContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.result(requestCode, resultCode);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.users_frag, container, false);
        ButterKnife.bind(this, root);
        // Set up users view

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);

        // Set up search bar
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            private Timer timer = new Timer();
            private final long DELAY = 500; // milliseconds

            @Override
            public void afterTextChanged(final Editable s) {
                searchTerm = s.toString();
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                mPresenter.search(searchTerm);
                            }
                        },
                        DELAY
                );
            }
        });

        // Set up progress indicator
        SwipeRefreshLayout swipeRefreshLayout =
                root.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (searchTerm.equals("")) {
                    mPresenter.loadUsers(true, true);
                } else {
                    mPresenter.search(searchTerm);
                }
            }
        });

        return root;
    }

    public class UserAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        private List<GitUser> userList = new ArrayList<>();
        private boolean isLoading = false;
        private int lastVisibleItem;
        private int totalItemCount;
        private int visibleThreshold = 5;
        private boolean noMoreRepositories = false;

        public void replaceData(List<GitUser> users) {
            userList = users;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        public void addData(List<GitUser> users) {
            removeProgressBarItem();
            userList.addAll(users);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }


        class UserViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.user_name_text_view)
            TextView userNameTextView;
            @BindView(R.id.url_text_view)
            TextView urlTextView;
            @BindView(R.id.user_id_text_view)
            TextView userIdTextView;
            @BindView(R.id.is_admin_text_view)
            TextView isAdminTextView;
            @BindView(R.id.user_icon_image_view)
            ImageView userIconImageView;


            UserViewHolder(ViewGroup itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class LoadingViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.progress_bar)
            ProgressBar progressBar;

            public LoadingViewHolder(ViewGroup itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }


        UserAdapter() {
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold) && !noMoreRepositories) {
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                loadMoreUsers();
                            }
                        });
                    }
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.user_item, parent, false);
                return new UserViewHolder(viewGroup);
            } else if (viewType == VIEW_TYPE_LOADING) {
                ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.layout_progress_bar, parent, false);
                return new LoadingViewHolder(viewGroup);
            }
            return null;
        }


        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof UserViewHolder) {
                UserViewHolder userViewHolder = (UserViewHolder) holder;
                GitUser gitUser = userList.get(position);
                userViewHolder.userNameTextView.setText(gitUser.getLogin() == null ? "" : gitUser.getLogin());
                userViewHolder.userIdTextView.setText(gitUser.getId() == null ? "" : gitUser.getId());
                userViewHolder.isAdminTextView.setText(String.valueOf(gitUser.isSiteAdmin()));
                userViewHolder.urlTextView.setText(String.valueOf(gitUser.getHtmlUrl()));
//                userViewHolder.userIconImageView.setImageBitmap(getGitUserBitmap(gitUser.getAvatarUrl()));
                new loadUserImage(gitUser.getAvatarUrl(),userViewHolder.userIconImageView).execute();
            } else if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

//        public Bitmap getGitUserBitmap(String src){
//          try{
//              URL url = new URL(src);
//              HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//              connection.setDoInput(true);
//              connection.connect();
//              InputStream input = connection.getInputStream();
//              Bitmap userImageBitmap = BitmapFactory.decodeStream(input);
//              return userImageBitmap;
//          }
//          catch (IOException e){
//              e.printStackTrace();
//              return null;
//          }
//        }

        @Override
        public int getItemViewType(int position) {
            if (isLoading && position == userList.size() - 1) {
                return VIEW_TYPE_LOADING;
            } else {
                return VIEW_TYPE_ITEM;
            }
        }

        void loadMoreUsers() {
            showProgressBarItem();
            mPresenter.loadMoreUsers();
        }


        private void showProgressBarItem() {
            isLoading = true;
            userList.add(new GitUser());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        private void removeProgressBarItem() {
            isLoading = false;
            int position = userList.size() - 1;
            if (position > -1) {
                userList.remove(position);
            }
        }

    }

    @Override
    public void setLoadingIndicator(final boolean active) {
        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl =
                getView().findViewById(R.id.swipe_refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void showNewUsers(List<GitUser> users) {
        mAdapter.replaceData(users);
    }

    @Override
    public void showMoreUsers(List<GitUser> users) {
        mAdapter.addData(users);
    }

    @Override
    public void noMoreUsers() {
        mAdapter.removeProgressBarItem();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void dataNotAvailable(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            String msg = getResources().getString(R.string.data_not_available) + "\n" + message;

            @Override
            public void run() {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
 // Load the user's profile image in backgroud thread
    private class loadUserImage extends AsyncTask<Void,Void,Bitmap>{
        private String mUrl;
        private ImageView mImageView;
        public loadUserImage(String url, ImageView imageView){
            this.mUrl = url;
            this.mImageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try{
                URL url = new URL(mUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap userImageBitmap = BitmapFactory.decodeStream(input);
                return userImageBitmap;
            }
            catch (IOException e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mImageView.setImageBitmap(bitmap);
        }
    }

}
