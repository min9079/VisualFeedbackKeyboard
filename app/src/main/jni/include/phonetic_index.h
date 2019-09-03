#ifndef __PHONETIC_INDEX_H__
#define __PHONETIC_INDEX_H__

#include <map>
#include <string>
#include <vector>

class PhoneticIndex {
public:
    const std::string m_pi_path;
    static std::string buildPIPath();
    PhoneticIndex(std::string pi_path, bool debug = false);
    virtual ~PhoneticIndex();
    std::vector<std::string> getPhoneticIndex(std::string phonetic_key);
    std::vector<std::string> getPhoneticKey(std::string word);

protected:
private:
    bool m_debug_;
    std::map<std::string, std::vector<std::string>> m_phonetic_index_;
};
#endif
