package com.example.violasclient;

import android.os.Bundle;

import com.diem.DiemClient;
import com.diem.DiemException;
import com.diem.Signer;
import com.diem.Testnet;
import com.diem.jsonrpc.JsonRpc;
import com.diem.jsonrpc.StaleResponseException;
import com.diem.stdlib.Helpers;
import com.diem.types.RawTransaction;
import com.diem.types.SignedTransaction;
import com.diem.types.TransactionPayload;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.violasclient.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.diem.*;
import com.diem.jsonrpc.StaleResponseException;
import com.diem.jsonrpc.JsonRpc;
import com.diem.types.RawTransaction;

import com.diem.stdlib.Helpers;
import com.diem.types.SignedTransaction;
import com.diem.types.TransactionPayload;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());

                executor.execute(() -> {
                    //Background work here
                    test_violas();

                    handler.post(() -> {
                        //UI Thread work here
                    });
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void test_violas() {


        try {
            DiemClient client = Testnet.createClient();
            LocalAccount parent = Utils.genAccount(client);
            JsonRpc.Account account = client.getAccount(parent.address);
            //Assert.assertNotNull(account);
            System.out.println("Parent VASP account:\n" + account);

            LocalAccount childVASP = LocalAccount.generate();
            long seq = client.getAccount(parent.address).getSequenceNumber();
            // it is recommended to set short expiration time for peer to peer transaction,
            // as Diem blockchain transaction execution is fast.
            Calendar expiration = Calendar.getInstance();
            expiration.add(Calendar.SECOND, 30);
            SignedTransaction txn = Signer.sign(parent.privateKey, new RawTransaction(
                    parent.address,
                    seq,
                    new TransactionPayload.Script(Helpers.encode_create_child_vasp_account_script(
                            Testnet.XUS_TYPE,
                            childVASP.address,
                            childVASP.authKey.prefix(),
                            false,
                            1000000l
                    )),
                    1000000l,
                    0l,
                    Testnet.XUS,
                    expiration.getTimeInMillis(),
                    Testnet.CHAIN_ID
            ));

            client.submit(txn);

            // WaitForTransaction retried for StaleResponseException
            // already, hence here we panic if got error (including timeout error)
            JsonRpc.Transaction transaction = client.waitForTransaction(txn, 30 * 1000);
            System.out.println("version: " + transaction.getVersion() + ", status: " + transaction.getVmStatus().getType());

            JsonRpc.Account childAccount = client.getAccount(childVASP.address);
            //Assert.assertNotNull(childAccount);
            System.out.println("Child VASP account:\n" + childAccount);
        } catch (StaleResponseException e) {
            // ignore stale response exception for submit.
            // submit probably succeed even hit a stale server.
        } catch (DiemException e) {
            //
        }
    }
}