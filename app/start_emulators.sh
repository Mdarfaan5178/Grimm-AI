curl -s -o /dev/null http://127.0.0.1:8085 || (setsid nohup firebase emulators:start --only firestore,auth --project demo-no-project > /tmp/firebase-emulators.log 2>&1 < /dev/null &)
for i in $(seq 1 90); do curl -s -o /dev/null http://127.0.0.1:8085 && curl -s -o /dev/null http://127.0.0.1:9099 && break; sleep 1; done
curl -s -o /dev/null http://127.0.0.1:8085 && curl -s -o /dev/null http://127.0.0.1:9099 && echo "Emulators ready" || { tail -n 50 /tmp/firebase-emulators.log; false; }
