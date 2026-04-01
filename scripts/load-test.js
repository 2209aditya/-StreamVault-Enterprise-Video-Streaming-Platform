import http from 'k6/http';
import { sleep } from 'k6';

export let options = {
  vus: 100, // virtual users
  duration: '1m'
};

export default function () {
  http.get('https://streamvault.com/videos');
  sleep(1);
}