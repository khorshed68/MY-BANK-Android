package com.khorshed.mybank.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.khorshed.mybank.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public LiveData<User> getCurrentUser() {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        
        if (firebaseUser != null) {
            db.collection("users")
                .document(firebaseUser.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        userLiveData.setValue(null);
                        return;
                    }
                    if (value != null && value.exists()) {
                        User user = value.toObject(User.class);
                        userLiveData.setValue(user);
                    }
                });
        }
        
        return userLiveData;
    }

    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        
        db.collection("users")
            .document(userId)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    userLiveData.setValue(null);
                    return;
                }
                if (value != null && value.exists()) {
                    User user = value.toObject(User.class);
                    userLiveData.setValue(user);
                }
            });
        
        return userLiveData;
    }

    public LiveData<List<User>> getAllUsers() {
        MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
        
        db.collection("users")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    usersLiveData.setValue(new ArrayList<>());
                    return;
                }
                if (value != null) {
                    List<User> users = value.toObjects(User.class);
                    usersLiveData.setValue(users);
                }
            });
        
        return usersLiveData;
    }

    public LiveData<List<User>> getUsersByRole(String role) {
        MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
        
        db.collection("users")
            .whereEqualTo("role", role)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    usersLiveData.setValue(new ArrayList<>());
                    return;
                }
                if (value != null) {
                    List<User> users = value.toObjects(User.class);
                    usersLiveData.setValue(users);
                }
            });
        
        return usersLiveData;
    }

    public void createUser(User user, OnCompleteListener listener) {
        db.collection("users")
            .document(user.getUserId())
            .set(user)
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void updateUser(String userId, Map<String, Object> updates, OnCompleteListener listener) {
        db.collection("users")
            .document(userId)
            .update(updates)
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void deleteUser(String userId, OnCompleteListener listener) {
        db.collection("users")
            .document(userId)
            .delete()
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void searchUsers(String query, OnSearchListener listener) {
        db.collection("users")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<User> results = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        String searchQuery = query.toLowerCase();
                        if (user.getName().toLowerCase().contains(searchQuery) ||
                            user.getEmail().toLowerCase().contains(searchQuery) ||
                            (user.getPhone() != null && user.getPhone().contains(searchQuery))) {
                            results.add(user);
                        }
                    }
                }
                listener.onSearchComplete(results);
            })
            .addOnFailureListener(e -> listener.onSearchComplete(new ArrayList<>()));
    }

    public void getUserByPhone(String phone, OnUserFetchListener listener) {
        db.collection("users")
            .whereEqualTo("phone", phone)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                    listener.onSuccess(user);
                } else {
                    listener.onSuccess(null);
                }
            })
            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void getUserByUsername(String username, OnUserFetchListener listener) {
        db.collection("users")
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                    listener.onSuccess(user);
                } else {
                    listener.onSuccess(null);
                }
            })
            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnSearchListener {
        void onSearchComplete(List<User> users);
    }

    public interface OnUserFetchListener {
        void onSuccess(User user);
        void onFailure(String error);
    }
}
