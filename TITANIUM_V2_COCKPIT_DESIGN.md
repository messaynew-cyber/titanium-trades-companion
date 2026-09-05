# Titanium Companion v2 — The Cockpit (Design Doc)
> Confirmed live data contract + verified public endpoints (2026-09-05).

## Mission
Turn the tested v1 companion into the premium REMOTE COCKPIT for the live Habesha/Titanium
two-engine system running on the Oracle VPS (129.80.112.9). Watch + alert + analyze — NOT a
reimplementation of the engine (HMM stays server-side: thermal rule on-device).

## Live read-only data (NO server changes, NO secrets in app)
- https://129.80.112.9/titanium_status.json  (public, app/json, ~51KB, refreshed ~60s)
- https://129.80.112.9/titanium_equity.json  (public, app/json, 5,851-pt curve from 2026-04-28)

## JSON contract (MUST parse exactly)
status.top = { updated, service:active, symbol, paper:bool, loop_seconds,
  equity:str, cash:str, last_cycle, last_status,
  regime: { regime:BULL/BEAR/CHOP, score, chop_prob, quality, action:BUY/HOLD/SELL,
            atr, last_price, rsi, hmm_state, hmm_prob },
  statistics: { today_count, today_pnl, today_win?, open_trade:{symbol,side,qty,entry,
                stop,take,opened_at,status:open,current_price},
    trade_history:[{symbol,side,qty,entry,stop,take,exit_price,exit_time,pnl,quality,
                regime,opened_at,status:open|closed}] } }
equity = [ {t:"YYYY-MM-DD HH:MM:SS", e:float}, ... ]  (downsample for sparkline)
symbols active = BTCUSD, ETHUSD, SOLUSD. paper acct equity ~$914.

## Cockpit UI (Compose, OLED obsidian + gold, glassmorphism — ADWA canon)
1 Engine status header  2 Per-symbol regime cards  3 Live open position (mark-to-market)
4 Today stats  5 Equity sparkline (5.8k->downsampled)  6 Risk/config pane  7 Signal feed
8 WorkManager alerts on regime change / SL-TP / new signal -> Android push.
Reuse v1 PositionRing + PriceSparkline + animated ticker components.

## Safety
Only reads public endpoints. Manual buy/sell NEVER from phone. Engine + control API untouched.
habesha.html dashboard untouched. VPS = read-only for this project (Adwa infra is read-only too).

## Phases
P1 decoder+fetcher (Kotlin/OkHttp, model classes match contract exactly, unit-test parse)
P2 Compose cockpit UI (biggest)
P3 alerts + adaptive icon (glass cockpit logo) + CI + install + iterate.
