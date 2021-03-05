# NekoX

NekoX is a **copylefted libre software** third-party Telegram client, based on Telegram-FOSS with features added.

Use, study, change and share; with all. Licensed GPLv3+.

[![Continuous integration](https://github.com/NekoX-Dev/NekoX/workflows/Debug%20build/badge.svg?branch=main)](https://github.com/NekoX-Dev/NekoX/actions) 
[![Translation status](https://hosted.weblate.org/widgets/nekox/-/svg-badge.svg)](https://hosted.weblate.org/engage/nekox/)

- [Google Play Store](https://play.google.com/store/apps/details?id=nekox.messenger)
- [Update News Telegram](https://t.me/NekogramX)
- [GitHub Feedback](https://github.com/NekoX-Dev/NekoX/issues)
- [Group Chat (English / Chinese)](https://t.me/NekoXChat) 
- [Group Chat (Persian)](https://t.me/NekogramX_Persian)
- [Group Chat (Indonesia)](https://t.me/NekoxID)

## NekoX Changes

- Most of Nekogram's features.
- Max account limit set to 16.
- OpenCC Chinese Convert.
- Built-in VMess, Shadowsocks, SSR, and Trojan-GFW proxy support.
- Built-in public proxy list / proxy subscription support.
- Parse all proxy subscription formats: SIP008, SSR, v2rayNG, vmess1, shit iOS app formats, clash config and more.
- Proxy import and export, remarks, speed measurement, sorting, delete unusable nodes, etc.
- Add proxies with QR codes.
- The (vmess / vmess1 / ss / SSR / Trojan) proxy link in the message can be clicked.
- Allow auto-disabling proxy when VPN is enabled.
- Proxy automatic switcher.
- Add stickers without sticker pack.
- Allow disabling vibration.
- Allow clicking on links in self profile.
- Sticker set list backup / restore / share.
- Full InstantView translation support.
- Translation support for selected text on input and in messages.
- Delete all messages in group.
- Dialog sorting is optional "Unread and can be prioritized for reminding" etc.
- Allow to skip "regret within five seconds".
- Unblock all users support.
- Log in via QR code.
- Scan and confirm the login QR code directly.
- Allow clearing app data.
- Option to not send comment first when forwarding.
- 0ption to use NekoX chat input menu: replace record button with a menu which contains an switch to control link preview (enabled by default).
- Option to disable link preview by default: to prevent the server from knowing that the link is shared through Telegram.
- Option to ignore Android-only content restrictions (except for the Play Store version).
- OpenKeychain client (sign / verify / decrypt / import).
- Google Cloud Translate / Yandex.Translate support.
- Custom cache directory (supports external storage).
- Custom app ID and Hash (optional NekoX / Andorid / Android X or Manual input).
- Custom server (official, test DC or Manual input).
- Keep the original file name when downloading files.
- View the data center you belong to when you don't have an avatar.
- Proxies, groups, channels, sticker packs are able to shared as QR codes.
- Force English emoji keywords to be loaded.
- Add "@Name" when long-pressing @user option.
- Enhanced notification service, optional version without Google Services.
- Don't alert "Proxy unavailable" for non-current account.
- Option to block others from starting a secret chat with you.
- Allow creation of group without invite.
- Option to upgrade group to supergroup.
- Mark dialogs as read using tab menu.
- Option to hide device info.
- Improved session dialog.
- Improved menu when long-clicking links.
- Text replacer.
- Option to disable trending.
- Telegram X style menu for unpinning messages.
- Built-in Material Design themes / Telegram X style icons.
- Auto-delete timer option for private chats and groups.
- Don't process cleanup draft events after opening chat.
- Save multiple selected messages to Saved Messages.
- Unpin multiple selected messages.
- View message stats.
- And more :)

## Compilation Guide

**Note: Building on Windows is, unfortunately, not supported.
Consider using a Linux VM or dual booting.**

**Important:**

1. Install the Android SDK and NDK (be default in $HOME/Android/SDK, otherwise you need to specify $ANDROID_HOME for it)

It is recommended to use [AndroidStudio](https://developer.android.com/studio) to install.

2. Install `golang` ( 1.15 ).
```shell
# debian sid
apt install -y golang
```

3. Install Rust and its stdlib for Android ABIs, and add environment variables for it.

It is recommended to use the official script, otherwise you may not find `rustup`.

```shell
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- --default-toolchain none -y
echo "source \$HOME/.cargo/env" >> $HOME/.bashrc && source $HOME/.cargo/env

rustup install $(cat ss-rust/src/main/rust/shadowsocks-rust/rust-toolchain)
rustup target install armv7-linux-androideabi aarch64-linux-android i686-linux-android x86_64-linux-android
```

4. Build native dependencies: `./run init libs`
5. Build external libraries and native code: `./run libs update`
6. Fill out `TELEGRAM_APP_ID` and `TELEGRAM_APP_HASH` in `local.properties`
7. Replace TMessagesProj/google-services.json if you want fcm to work.
8. Replace release.keystore with yours and fill out `ALIAS_NAME`, `KEYSTORE_PASS` and `ALIAS_PASS` in `local.properties` if you want a non-debug build.

`./gradlew assemble<Full/Mini><Debug/Release/ReleaseNoGcm>`

## FAQ

#### What is the relationship between NekoX and Nekogram?

More features, **without** [additional trackers](https://gitlab.com/search?utf8=%E2%9C%93&snippets=false&scope=&repository_ref=master&search=AnalyticsHelper&group_id=10273976&project_id=22804922).

#### What is the difference between the Full and Mini version?

The full version comes with built-in proxy support for V2Ray, shadowsocks, shadowsocksr, and Trojan, which is usually provided to advanced users to help friends who have no computer knowledge in mainland China to bypass censorship. Don't complain about imperfect functions or ask to add other rare proxy types, you can use their clients directly.

#### What if I don't need a proxy?

Then it is recommended to use the `Mini` version.

#### I've encountered a bug!

First, make sure you have the latest version installed (check the channel, Play store versions usually have a delay).

Then, if the issue appears in the official Telegram client too, please submit it to the officials, (be careful not to show NekoX in the description and screenshots, the official developers doesn't like us!).

Then, please *detail* your issue, create an issue or submit it to our [group](https://t.me/NekoXChat) with #bug.

If you experience a *crash*, you also need to click on the version number at the bottom of the settings and select "Enable Log" and send it to us.

## Localization

Help translate NekoX to your language, or fix translations at [Hosted Weblate](https://hosted.weblate.org/engage/nekox/).

[![Translation status](https://hosted.weblate.org/widgets/nekox/-/horizontal-auto.svg)](https://hosted.weblate.org/engage/nekox/)

### Adding a new language

Android must already support the specific language and locale you want to add. When trying to work with languages Android and the SDK don't support, the tools simply break down. Next, if you are considering adding a country-specific variant of a language (e.g. de-AT), first make sure the main language is well maintained (e.g. de). Your contribution might be useful to more people if you contribute to the existing version of your language rather than doing more in the country-specific variant.

Anyone can [create a new language](https://hosted.weblate.org/new-lang/nekox/nekox/) on Hosted Weblate.

### Adding unofficial translations for Telegram

Current built-in language packs:

* 简体中文: [moecn](https://translations.telegram.org/moecn)
* 正體中文: [taiwan](https://translations.telegram.org/taiwan)
* 日本語: [ja_raw](https://translations.telegram.org/ja_raw)

You can [open an issue](https://github.com/NekoX-Dev/NekoX/issues/new?&template=language_request.md) to request to amend the built-in translation.

## Credits

<ul>
    <li>Telegram-FOSS: <a href="https://github.com/Telegram-FOSS-Team/Telegram-FOSS/blob/master/LICENSE">GPLv2</a></li>
    <li>Nekogram: <a href="https://gitlab.com/Nekogram/Nekogram/-/blob/master/LICENSE">GPLv2</a></li>
    <li>v2rayNG: <a href="https://github.com/2dust/v2rayNG/blob/master/LICENSE">GPLv3</a></li>
    <li>AndroidLibV2rayLite: <a href="https://github.com/2dust/AndroidLibV2rayLite/blob/master/LICENSE">LGPLv3</a></li>
    <li>shadowsocks-android: <a href="https://github.com/shadowsocks/shadowsocks-android/blob/master/LICENSE">GPLv3</a></li>
    <li>shadowsocksRb-android: <a href="https://github.com/shadowsocksRb/shadowsocksRb-android/blob/master/LICENSE">GPLv3</a></li>
    <li>HanLP: <a href="https://github.com/hankcs/HanLP/blob/1.x/LICENSE">Apache License 2.0</a></li>
    <li>OpenCC: <a href="https://github.com/BYVoid/OpenCC/blob/master/LICENSE">Apache License 2.0</a></li>
    <li>opencc-data: <a href="https://github.com/nk2028/opencc-data">Apache License 2.0</a></li>
    <li>android-device-list: <a href="https://github.com/pbakondy/android-device-list/blob/master/LICENSE">MIT</a> </li>
</ul>
