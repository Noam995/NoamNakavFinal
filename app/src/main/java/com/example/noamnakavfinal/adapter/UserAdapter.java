package com.example.noamnakavfinal.adapter;

// ייבוא ספריות נדרשות של אנדרואיד (הקשר, מעבר מסכים, תצוגה) ושל הפרויקט
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noamnakavfinal.R;
import com.example.noamnakavfinal.UserDetailsActivity; // המסך שאליו נעבור בעת לחיצה על משתמש
import com.example.noamnakavfinal.model.User;

import java.util.ArrayList;

// מחלקת המתאם (Adapter) להצגת משתמשים.
// יורשת מ-RecyclerView.Adapter ומשתמשת במחלקה פנימית בשם UserViewHolder כדי לנהל את התצוגה ביעילות.
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    // --- משתני המחלקה ---
    // רשימת המשתמשים שתוצג על המסך
    ArrayList<User> users;
    // ההקשר (Context) של המסך הנוכחי - מאפשר לנו לבצע פעולות כמו טעינת עיצוב ומעבר בין מסכים
    Context context;

    // --- פעולה בונה (Constructor) ---
    // מופעלת כשאנחנו יוצרים את המתאם במסך (למשל ב-UserList) ומקבלת את הרשימה וההקשר
    public UserAdapter(ArrayList<User> users, Context context) {
        this.users = users;
        this.context = context;
    }

    // --- יצירת תבנית התצוגה (ViewHolder) ---
    // פונקציה זו מופעלת בכל פעם שהרשימה צריכה לייצר "שורה" חדשה על המסך
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "מנפחים" (טוענים) את קובץ ה-XML של שורת משתמש בודדת (row_user.xml)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_user, parent, false);

        // מחזירים את השורה כשהיא עטופה ב-ViewHolder (שישמור את הרכיבים שלה למניעת חיפושים חוזרים)
        return new UserViewHolder(view);
    }

    // --- חיבור הנתונים לתצוגה ---
    // מופעלת עבור כל שורה ומכניסה אליה את הנתונים המדויקים של אותו משתמש
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        // שולפים את המשתמש הספציפי מהרשימה לפי המיקום (position) שלו
        User user = users.get(position);

        // מציבים את הנתונים מהאובייקט אל תוך תיבות הטקסט שבשורה
        holder.tvName.setText(user.getFname() + " " + user.getLname()); // חיבור השם הפרטי ושם המשפחה
        holder.tvEmail.setText(user.getEmail()); // אימייל
        holder.tvPhone.setText(user.getPhone()); // מספר טלפון

        // בדיקת תפקיד: אם הפונקציה isAdmin מחזירה true נכתוב "Admin", אחרת נכתוב "User"
        // זה נקרא "תנאי מקוצר" (Ternary Operator)
        holder.tvAdmin.setText(user.isAdmin() ? "Admin" : "User");

        // --- לחיצה על שורה ---
        // מגדיר מה קורה כשהמנהל לוחץ על כרטיסיית המשתמש כולה (itemView)
        holder.itemView.setOnClickListener(v -> {
            // יצירת Intent כדי לעבור למסך פרטי המשתמש (UserDetailsActivity)
            Intent intent = new Intent(context, UserDetailsActivity.class);

            // "אורזים" את ה-ID של המשתמש שנלחץ ומעבירים אותו למסך הבא
            // ככה המסך הבא ידע איזה משתמש למשוך ממסד הנתונים כדי להציג את פרטיו או למחוק אותו
            intent.putExtra("USER_ID", user.getId());

            // מתחילים את המעבר למסך החדש
            context.startActivity(intent);
        });
    }

    // --- ספירת פריטים ---
    // מחזירה למערכת כמה משתמשים יש בסך הכל ברשימה כדי שתדע כמה שורות לייצר
    @Override
    public int getItemCount() {
        return users.size();
    }

    // --- מחלקה פנימית: UserViewHolder ---
    // מחלקה זו שומרת "מטמון" (Cache) של רכיבי התצוגה (תיבות הטקסט) בתוך שורה אחת.
    // זה מייעל את האפליקציה, כיוון שלא צריך לבצע את הפעולה הכבדה findViewById בכל פעם שגוללים את הרשימה.
    static class UserViewHolder extends RecyclerView.ViewHolder {
        // המשתנים של רכיבי התצוגה בשורה
        TextView tvName, tvEmail, tvPhone, tvAdmin;

        // הפעולה הבונה מקבלת שורה אחת שלמה (itemView) מהרשימה
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // מאתרת את תיבות הטקסט מתוך השורה ומקשרת אותן למשתנים
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAdmin = itemView.findViewById(R.id.tvAdmin);
        }
    }
}