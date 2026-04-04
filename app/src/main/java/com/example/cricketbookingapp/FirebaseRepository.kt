package com.example.cricketbookingapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object FirebaseRepository {
    private const val USERS_COLLECTION = "users"
    private const val EVENTS_COLLECTION = "events"

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun isUserSignedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error ->
                onError(error.localizedMessage ?: "sign_in_failed")
            }
    }

    fun signUp(
        firstName: String,
        lastName: String,
        mobileNumber: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection(USERS_COLLECTION)
            .whereEqualTo("mobileNumber", mobileNumber)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    onError("Mobile number already registered")
                    return@addOnSuccessListener
                }

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        val uid = result.user?.uid.orEmpty()
                        val profile = hashMapOf(
                            "uid" to uid,
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "mobileNumber" to mobileNumber,
                            "email" to email,
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        firestore.collection(USERS_COLLECTION)
                            .document(uid)
                            .set(profile)
                            .addOnSuccessListener {
                                auth.signOut()
                                onSuccess()
                            }
                            .addOnFailureListener { error ->
                                onError(error.localizedMessage ?: "sign_up_failed")
                            }
                    }
                    .addOnFailureListener { error ->
                        onError(error.localizedMessage ?: "sign_up_failed")
                    }
            }
            .addOnFailureListener { error ->
                onError(error.localizedMessage ?: "sign_up_failed")
            }
    }

    fun loadCurrentUserProfile(
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = getCurrentUserId()
        if (uid.isNullOrBlank()) {
            onError("No signed in user")
            return
        }

        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val profile = UserProfile(
                    uid = snapshot.getString("uid").orEmpty(),
                    firstName = snapshot.getString("firstName").orEmpty(),
                    lastName = snapshot.getString("lastName").orEmpty(),
                    mobileNumber = snapshot.getString("mobileNumber").orEmpty(),
                    email = snapshot.getString("email").orEmpty()
                )
                onSuccess(profile)
            }
            .addOnFailureListener { error ->
                onError(error.localizedMessage ?: "Unable to load profile")
            }
    }

    fun listenToBookings(
        onUpdate: (List<BookingItem>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return firestore.collection(EVENTS_COLLECTION)
            .orderBy("startDateTimeMillis")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Unable to load events")
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { document ->
                    val bookingName = document.getString("bookingName").orEmpty()
                    val startDateTimeMillis = document.getLong("startDateTimeMillis") ?: return@mapNotNull null
                    val endDateTimeMillis = document.getLong("endDateTimeMillis") ?: return@mapNotNull null
                    val amount = document.getString("amount").orEmpty()
                    val createdByName = document.getString("createdByName").orEmpty()

                    BookingItem(
                        id = document.id,
                        name = bookingName,
                        startDateTimeMillis = startDateTimeMillis,
                        endDateTimeMillis = endDateTimeMillis,
                        amount = amount,
                        createdByName = createdByName
                    )
                }.orEmpty()

                onUpdate(items)
            }
    }

    fun addBooking(
        bookingName: String,
        startDateTimeMillis: Long,
        endDateTimeMillis: Long,
        amount: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = getCurrentUserId()
        if (uid.isNullOrBlank()) {
            onError("No signed in user")
            return
        }

        loadCurrentUserProfile(
            onSuccess = { profile ->
                val payload = hashMapOf(
                    "bookingName" to bookingName,
                    "startDateTimeMillis" to startDateTimeMillis,
                    "endDateTimeMillis" to endDateTimeMillis,
                    "amount" to amount,
                    "createdByUid" to uid,
                    "createdByName" to profile.fullName,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                firestore.collection(EVENTS_COLLECTION)
                    .add(payload)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { error ->
                        onError(error.localizedMessage ?: "Unable to add booking")
                    }
            },
            onError = onError
        )
    }
}
