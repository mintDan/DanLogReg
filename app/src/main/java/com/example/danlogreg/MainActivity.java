package com.example.danlogreg;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import Jama.Matrix;

public class MainActivity extends AppCompatActivity {

    LineGraphSeries<DataPoint> GraphDataPoints;
    LineGraphSeries<DataPoint> GraphFunction;
    private Button btnClearGraph, btnLogReg;

    DBHelper DB;
    EditText yET;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DB = new DBHelper(this);


        GraphView graphmnd = (GraphView) findViewById(R.id.graphmnd);




        //Run an initial LogReg onCreate
        double[][] xdat ={{1,0.50},{1,0.75},{1,1.00},{1,1.25},{1,1.50},{1,1.75},{1,1.75},{1,2.00},{1,2.25},{1,2.50},{1,2.75},{1,3.00},{1,3.25 },{1,3.50},{1,4.00},{1,4.25},{1,4.50},{1,4.75},{1,5.00},{1,5.50}};
        double[][] ydat = {{0},{0},{0},{0},{0},{0},{1},{0},{1},{0},{1},{0},{1},{0},{1},{1},{1},{1},{1},{1}};
        //double[][] xdat ={{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,30},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0}};
        // double[][] ydat = {{0},{0},{0},{0},{0},{0},{0},{0},{0},{0},{0},{0},{1},{0},{0},{0},{0},{0},{0},{0}};

/*        //Try fill arrays from SQL
        Cursor res = DB.getdata("SchoolExams");
        if (res.getCount() == 0) {
            Toast.makeText(MainActivity.this,"No Data",Toast.LENGTH_LONG).show();
            return;
        }
        //StringBuffer buffer = new StringBuffer();
        int nc = 0;
        while(res.moveToNext()){

            xdat[nc][1] = res.getDouble(0);
            ydat[nc][0] = res.getDouble(1);
            nc = nc+1;
        }*/


        double[][] muarr = new double[20][1];
        double[][] warr = {{-3},{1}};
        Matrix X = new Matrix(xdat);
        Matrix y = new Matrix(ydat);
        Matrix mu = new Matrix(muarr);
        Matrix w = new Matrix(warr);

        for (int i = 0; i < 20; i++) {
            double tmp1 = w.get(0,0)*X.get(i,0)+w.get(1,0)*X.get(i,1);
            double fsigmoid = 1/(1+Math.exp(-tmp1));
            mu.set(i,0,fsigmoid);
        }
        Matrix S = new Matrix(20,20); //matrix of zeros
        for (int i = 0; i < 20; i++) {
            S.set(i,i,mu.get(i,0)*(1-mu.get(i,0)));
        }

        //Start IRLS Loop
        for (int k = 0; k < 100; k++) {
            //here, get the max log likelihood coeffs by iterating
            //Actually SX is on both LHS and RHS, so could actually use less computation
            Matrix XTSX = X.transpose().times(S).times(X);
            Matrix XTSXinv = XTSX.inverse();
            Matrix XTSXinvXT = XTSXinv.times(X.transpose());

            Matrix SX = S.times(X);
            Matrix SXw = SX.times(w);

            Matrix RHS = SXw.plus(y).minus(mu);

            w = XTSXinvXT.times(RHS);

            //Update mu
            for (int i = 0; i < 20; i++) {
                double tmp1 = w.get(0,0)*X.get(i,0)+w.get(1,0)*X.get(i,1);
                double fsigmoid = 1/(1+Math.exp(-tmp1));
                mu.set(i,0,fsigmoid);
            }

            //Update S
            for (int i = 0; i < 20; i++) {
                S.set(i,i,mu.get(i,0)*(1-mu.get(i,0)));
            }

        }


        double b0 = w.get(0, 0);
        double b1 = w.get(1, 0);

        //Set table values
        TextView tv11 =(TextView)findViewById(R.id.t21);
        tv11.setText(Double.toString(b0));
        TextView tv12 =(TextView)findViewById(R.id.t22);
        tv12.setText(Double.toString(b1));



        //Add points to graph
        GraphDataPoints = new LineGraphSeries<DataPoint>();
        for (int i = 0; i < 20; i++) {
            double x1 = xdat[i][1];
            double y1 = ydat[i][0];
            GraphDataPoints.appendData(new DataPoint(x1, y1), true, 20);
        }

        graphmnd.addSeries(GraphDataPoints);

        //Plot found function
        GraphFunction = new LineGraphSeries<DataPoint>();
        double x0 = 0;
        for (int i = 0; i < 60; i++) {
            x0 = x0+0.1;
            double tmp1 = w.get(0,0)*1+w.get(1,0)*x0;
            double fsigmoid = 1/(1+Math.exp(-tmp1));
            GraphFunction.appendData(new DataPoint(x0, fsigmoid), true, 60);
        }
        graphmnd.addSeries(GraphFunction);
        GraphDataPoints.setDrawDataPoints(true);
        GraphDataPoints.setDataPointsRadius(10f);
        GraphDataPoints.setThickness(0);
        GraphFunction.setThickness(8);
        GraphFunction.setColor(Color.BLACK);

        graphmnd.setTitle("Data points and logistic function");
        double MinX = -1;
        double MaxX = 7;
        double MinY = 0;
        double MaxY = 1;
        graphmnd.getViewport().setMinX(MinX);
        graphmnd.getViewport().setMaxX(MaxX);
        graphmnd.getViewport().setMinY(MinY);
        graphmnd.getViewport().setMaxY(MaxY);

        graphmnd.getViewport().setYAxisBoundsManual(true);
        graphmnd.getViewport().setXAxisBoundsManual(true);



        btnClearGraph = (Button) findViewById(R.id.btnClearGraph);
        btnLogReg = (Button) findViewById(R.id.btnLogReg);




        btnClearGraph.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                graphmnd.removeAllSeries();
            }

        });

        btnLogReg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                LogReg();
            }
        });


    }




    public void LogReg(){
        double[][] xdat ={{1,0.50},{1,0.75},{1,1.00},{1,1.25},{1,1.50},{1,1.75},{1,1.75},{1,2.00},{1,2.25},{1,2.50},{1,2.75},{1,3.00},{1,3.25 },{1,3.50},{1,4.00},{1,4.25},{1,4.50},{1,4.75},{1,5.00},{1,5.50}};
        double[][] ydat = {{0},{0},{0},{0},{0},{0},{1},{0},{1},{0},{1},{0},{1},{0},{1},{1},{1},{1},{1},{1}};
        //double[][] xdat ={{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0},{1,30},{1,0},{1,0},{1,0},{1,0},{1,0},{1,0}};
       // double[][] ydat = {{0},{0},{0},{0},{0},{0},{0},{0},{0},{0},{0},{0},{1},{0},{0},{0},{0},{0},{0},{0}};

/*        //Try fill arrays from SQL
        Cursor res = DB.getdata("SchoolExams");
        if (res.getCount() == 0) {
            Toast.makeText(MainActivity.this,"No Data",Toast.LENGTH_LONG).show();
            return;
        }
        //StringBuffer buffer = new StringBuffer();
        int nc = 0;
        while(res.moveToNext()){

            xdat[nc][1] = res.getDouble(0);
            ydat[nc][0] = res.getDouble(1);
            nc = nc+1;
        }*/


        double[][] muarr = new double[20][1];
        double[][] warr = {{-3},{1}};
        Matrix X = new Matrix(xdat);
        Matrix y = new Matrix(ydat);
        Matrix mu = new Matrix(muarr);
        Matrix w = new Matrix(warr);

        for (int i = 0; i < 20; i++) {
            double tmp1 = w.get(0,0)*X.get(i,0)+w.get(1,0)*X.get(i,1);
            double fsigmoid = 1/(1+Math.exp(-tmp1));
            mu.set(i,0,fsigmoid);
        }
        Matrix S = new Matrix(20,20); //matrix of zeros
        for (int i = 0; i < 20; i++) {
            S.set(i,i,mu.get(i,0)*(1-mu.get(i,0)));
        }

        //Start IRLS Loop
        for (int k = 0; k < 100; k++) {
            //here, get the max log likelihood coeffs by iterating
            //Actually SX is on both LHS and RHS, so could actually use less computation
            Matrix XTSX = X.transpose().times(S).times(X);
            Matrix XTSXinv = XTSX.inverse();
            Matrix XTSXinvXT = XTSXinv.times(X.transpose());

            Matrix SX = S.times(X);
            Matrix SXw = SX.times(w);

            Matrix RHS = SXw.plus(y).minus(mu);

            w = XTSXinvXT.times(RHS);

            //Update mu
            for (int i = 0; i < 20; i++) {
                double tmp1 = w.get(0,0)*X.get(i,0)+w.get(1,0)*X.get(i,1);
                double fsigmoid = 1/(1+Math.exp(-tmp1));
                mu.set(i,0,fsigmoid);
            }

            //Update S
            for (int i = 0; i < 20; i++) {
                S.set(i,i,mu.get(i,0)*(1-mu.get(i,0)));
            }

        }


        double b0 = w.get(0, 0);
        double b1 = w.get(1, 0);

 /*       //Set table values
        TextView tv11 =(TextView)findViewById(R.id.t21);
        tv11.setText(Double.toString(b0));
        TextView tv12 =(TextView)findViewById(R.id.t22);
        tv12.setText(Double.toString(b1));*/



        //Add points to graph
        GraphDataPoints = new LineGraphSeries<DataPoint>();
        for (int i = 0; i < 20; i++) {
            double x1 = xdat[i][1];
            double y1 = ydat[i][0];
            GraphDataPoints.appendData(new DataPoint(x1, y1), true, 20);
        }
        GraphView graphmnd = (GraphView) findViewById(R.id.graphmnd);
        graphmnd.addSeries(GraphDataPoints);

        //Plot found function
        GraphFunction = new LineGraphSeries<DataPoint>();
        double x0 = 0;
        for (int i = 0; i < 60; i++) {
            x0 = x0+0.1;
            double tmp1 = w.get(0,0)*1+w.get(1,0)*x0;
            double fsigmoid = 1/(1+Math.exp(-tmp1));
            GraphFunction.appendData(new DataPoint(x0, fsigmoid), true, 60);
        }
        graphmnd.addSeries(GraphFunction);
        GraphDataPoints.setDrawDataPoints(true);
        GraphDataPoints.setDataPointsRadius(10f);
        GraphDataPoints.setThickness(0);
        GraphFunction.setThickness(8);
        GraphFunction.setColor(Color.BLACK);

        graphmnd.setTitle("Data points and logistic function");
        double MinX = -1;
        double MaxX = 7;
        double MinY = 0;
        double MaxY = 1;
        graphmnd.getViewport().setMinX(MinX);
        graphmnd.getViewport().setMaxX(MaxX);
        graphmnd.getViewport().setMinY(MinY);
        graphmnd.getViewport().setMaxY(MaxY);

        graphmnd.getViewport().setYAxisBoundsManual(true);
        graphmnd.getViewport().setXAxisBoundsManual(true);

    } //ends LogReg()

}