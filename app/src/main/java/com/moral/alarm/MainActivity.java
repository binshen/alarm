package com.moral.alarm;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.moral.alarm.adapter.AlarmAdapter;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private AlarmAdapter mAlarmAdapter;
    private ListView mLvAlarm;

    private Thread subscribeThread;
    private ConnectionFactory factory;

    private List<JSONObject> alarmList;

    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;

    private TextView mTvTitle;
    private ImageView mIvLeft;

    private long clickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmList = new ArrayList<>();

        mLvAlarm = (ListView) findViewById(R.id.lv_alarm);
        mLvAlarm.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "设计开发中......", Toast.LENGTH_SHORT).show();
            }
        });

        factory = new ConnectionFactory();
        factory.setHost("121.40.92.176");

        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm);

        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvTitle.setText("报警列表");

        mIvLeft = (ImageView) findViewById(R.id.left_btn);
        mIvLeft.setVisibility(View.GONE);

        final Handler messageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                try {
                    alarmList.add(0, new JSONObject(message));

                    mAlarmAdapter = new AlarmAdapter(getApplicationContext(), alarmList);
                    mLvAlarm.setAdapter(mAlarmAdapter);
                    mAlarmAdapter.notifyDataSetChanged();

                    mediaPlayer.start();
                    //vibrator.vibrate(10000);
                    vibrator.vibrate(new long[] { 1000, 780, 1000, 780, 1000, 780, 1000, 780, 1000, 780, 1000, 780 }, -1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        subscribe(messageHandler);
    }

    private void subscribe(final Handler handler) {
        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.exchangeDeclare("logs", "fanout");
                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, "logs", "");
                Log.d("MainActivity"," [*] Waiting for messages. To exit press CTRL+C");

                Consumer consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String message = new String(body, "UTF-8");
                        Log.d("MainActivity"," [x] Received '" + message + "'");

                        Message msg = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("msg", message);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                };

                channel.basicConsume(queueName, true, consumer);
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        });
        subscribeThread.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - clickTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次后退键退出程序", Toast.LENGTH_SHORT).show();
                clickTime = System.currentTimeMillis();
            } else {
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
