package com.vpncheck;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultText = findViewById(R.id.resultText);
        Button btn = findViewById(R.id.checkBtn);
        btn.setOnClickListener(v -> checkVpn());
        checkVpn();
    }

    private void checkVpn() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== VPN CHECK ===\n\n");
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            Network net = cm.getActiveNetwork();
            if (net == null) { sb.append("No active network"); resultText.setText(sb); return; }
            NetworkCapabilities caps = cm.getNetworkCapabilities(net);
            if (caps == null) { sb.append("caps = null"); resultText.setText(sb); return; }

            boolean isVpn  = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
            boolean isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            boolean isCell = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            boolean notVpn = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN);

            sb.append("TRANSPORT_VPN:  ").append(isVpn  ? "TRUE  <- VPN VISIBLE!" : "FALSE <- clean").append("\n");
            sb.append("TRANSPORT_WIFI: ").append(isWifi).append("\n");
            sb.append("TRANSPORT_CELL: ").append(isCell).append("\n");
            sb.append("NOT_VPN cap:    ").append(notVpn ? "TRUE (normal)" : "FALSE <- VPN!").append("\n\n");

            try {
                Object ti = caps.getTransportInfo();
                sb.append("TransportInfo: ").append(ti != null ? "SET: " + ti.getClass().getSimpleName() : "null (no VPN info)").append("\n\n");
            } catch (Exception e) { sb.append("TransportInfo: n/a\n\n"); }

            sb.append("--- INTERFACES ---\n");
            List<NetworkInterface> ifaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface iface : ifaces) {
                if (iface.isUp()) {
                    String name = iface.getName();
                    String flag = name.matches(".*(tun|wg|amn|tap|ipsec|ppp).*") ? " <- VPN!" : "";
                    sb.append("  ").append(name).append(flag).append("\n");
                }
            }

            sb.append("\n--- RESULT ---\n");
            if (isVpn || !notVpn) {
                sb.append("VPN DETECTED in this profile!");
            } else {
                sb.append("VPN NOT detected. Knox isolates ok.");
            }
        } catch (Exception e) { sb.append("Error: ").append(e.getMessage()); }
        resultText.setText(sb.toString());
    }
}