package com.adan.blog.service;

import com.adan.blog.entity.Setting;
import com.adan.blog.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingRepository settingRepository;

    private static final String[] KEYS = {
            "site_title", "site_subtitle", "site_description", "author_name",
            "author_bio", "author_avatar", "github_url", "twitter_url", "email", "icp"
    };

    public Map<String, String> getPublicSettings() {
        Map<String, String> result = new LinkedHashMap<>();
        for (String key : KEYS) {
            result.put(key, settingRepository.findById(key).map(Setting::getValue).orElse(""));
        }
        return result;
    }

    public void updateSettings(Map<String, String> settings) {
        for (String key : KEYS) {
            String value = settings.get(key);
            if (value == null) continue;
            Setting s = settingRepository.findById(key).orElseGet(() -> {
                Setting ns = new Setting();
                ns.setKey(key);
                return ns;
            });
            s.setValue(value);
            settingRepository.save(s);
        }
    }
}
