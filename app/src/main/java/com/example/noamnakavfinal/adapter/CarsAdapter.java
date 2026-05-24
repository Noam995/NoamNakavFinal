package com.example.noamnakavfinal.adapter;

// ייבוא ספריות נדרשות של אנדרואיד (הקשר, תצוגה, תמונות) ושל הפרויקט
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noamnakavfinal.R;
import com.example.noamnakavfinal.model.Car;
import com.example.noamnakavfinal.util.ImageUtil;

import java.util.List;

// מחלקת המתאם. יורשת מ-RecyclerView.Adapter ומשתמשת במחלקה פנימית CarViewHolder
public class CarsAdapter extends RecyclerView.Adapter<CarsAdapter.CarViewHolder> {

    // --- הגדרת ממשק (Interface) לטיפול בלחיצות ---
    // מאפשר למסך שמשתמש במתאם הזה (כמו SearchAllCars) להגדיר מה יקרה כשלוחצים על רכב
    public interface OnCarClickListener {
        void onClick(Car car); // פונקציה שתופעל ותעביר את הרכב שנלחץ
    }

    // --- משתני המחלקה ---
    Context context; // ההקשר של המסך בו הרשימה מוצגת (דרוש כדי ליצור רכיבי תצוגה)
    List<Car> cars; // רשימת הרכבים שתוצג
    OnCarClickListener listener; // המאזין ללחיצות שהוגדר

    // פעולה בונה (Constructor) - מופעלת כשיוצרים את המתאם במסך
    public CarsAdapter(Context context, List<Car> cars, OnCarClickListener listener) {
        this.context = context;
        this.cars = cars;
        this.listener = listener;
    }

    // פונקציה שמאפשרת לעדכן את הרשימה מבחוץ (למשל אחרי סינון או מיון)
    public void updateList(List<Car> newCars) {
        cars = newCars; // מחליף את הרשימה הישנה בחדשה
        notifyDataSetChanged(); // מודיע ל-RecyclerView שהנתונים השתנו ושעליו לרענן את התצוגה
    }

    // פונקציה זו מופעלת כשה-RecyclerView צריך לייצר "כרטיסייה" חדשה על המסך
    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // מנפח (טוען) את קובץ העיצוב של כרטיסיית רכב בודד (car_item.xml)
        View view = LayoutInflater.from(context)
                .inflate(R.layout.car_item, parent, false);

        // מחזיר את הכרטיסייה החדשה עטופה ב-ViewHolder (שישמור את הרכיבים שלה)
        return new CarViewHolder(view);
    }

    // פונקציה זו מופעלת לכל כרטיסייה, ותפקידה לחבר את הנתונים (הטקסטים והתמונות) אל התצוגה
    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        // שולף את הרכב הספציפי מהרשימה לפי המיקום (position) שלו
        Car car = cars.get(position);

        // מציב את הנתונים של הרכב בתוך תיבות הטקסט שבכרטיסייה
        holder.title.setText(car.getBrand() + " " + car.getModel()); // חיבור יצרן ודגם
        holder.price.setText("₪ " + car.getPrice()); // מחיר עם סמל שקל
        holder.year.setText("שנה: " + car.getYear());

        // טיפול בתמונה: אם קיימת מחרוזת תמונה (Base64) עבור הרכב הזה
        if (car.getImage64() != null) {
            // ממיר את המחרוזת בחזרה לתמונה חיה (Bitmap) בעזרת מחלקת העזר
            Bitmap bitmap = ImageUtil.convertFrom64base(car.getImage64());
            // מציב את התמונה ב-ImageView של הכרטיסייה
            holder.img.setImageBitmap(bitmap);
        }

        // מגדיר מה קורה שלוחצים על הכרטיסייה כולה (itemView)
        holder.itemView.setOnClickListener(v -> {
            // מפעיל את הפונקציה בממשק ומעביר את הרכב שעליו לחצו למסך הראשי
            listener.onClick(car);
        });
    }

    // פונקציה שמחזירה ל-RecyclerView את כמות הפריטים שיש להציג בסך הכל
    @Override
    public int getItemCount() {
        return cars.size();
    }

    // --- מחלקה פנימית: CarViewHolder ---
    // תפקידה לשמור "מטמון" של רכיבי התצוגה של כרטיסייה אחת.
    // זה מונע מהאפליקציה לבצע חיפוש (findViewById) שוב ושוב בזמן גלילה ומייעל מאוד את הביצועים.
    static class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView title, price, year;

        // הפעולה הבונה מקבלת כרטיסייה אחת שלמה (itemView) ומחלצת מתוכה את הרכיבים
        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgCar);
            title = itemView.findViewById(R.id.tvTitle);
            price = itemView.findViewById(R.id.tvPrice);
            year = itemView.findViewById(R.id.tvYear);
        }
    }
}