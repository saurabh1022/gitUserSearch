package com.gitTask.gitusers.Users;

import com.gitTask.gitusers.BasePresenter;
import com.gitTask.gitusers.BaseView;
import com.gitTask.gitusers.data.GitUser;

import java.util.List;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface UserContract {

    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showNewUsers(List<GitUser> users);

        void showMoreUsers(List<GitUser> users);

        void noMoreUsers();

        void dataNotAvailable(String message);
    }

    interface Presenter extends BasePresenter {

        void result(int requestCode, int resultCode);

        void loadUsers(boolean forceUpdate, boolean showLoadingUI);

        void loadMoreUsers();

        void search(String searchTerm);
    }
}
