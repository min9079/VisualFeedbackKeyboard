#ifndef __ERROR_POSITION_INFO_H__
#define __ERROR_POSITION_INFO_H__

#include <string>
#include <vector>

class ErrorPositionInfo {
public:
    const std::string m_ep_path;
    static std::string buildEpPath();

    ErrorPositionInfo(std::string ep_path, bool debug = false);
    virtual ~ErrorPositionInfo();
    std::vector<std::string> getErrorModels();
    float getErrorProb(int word_len, int char_position, std::string model_name = "msr_misspellings");

protected:
private:
    bool m_debug_;
};
#endif
