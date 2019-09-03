#ifndef __EM_QUERY_H__
#define __EM_QUERY_H__

#include "error_position_info.h"
#include "lm_query.h"
#include <string>

class EmQuery {
public:
    static const std::string EM_PATH;
    static std::string buildEmPath(std::string em_name = "all", int order = 4);

    EmQuery(std::string em_path, float ncer = 0.02, bool show_words = true);
    virtual ~EmQuery();
    bool isShowWords();
    void setShowWords(bool show_words);
    float calcEmProb(std::string v_word, std::string w_word);
    float calcCerrorProb(std::string query_string);
    int getOrder();
    virtual float getCerFromNcer(int char_position, int word_len);

protected:
private:
    LmQuery* m_lm_query_;
    int m_order_;
    const float m_ncer_;
    ErrorPositionInfo m_error_position_info_;
    bool m_show_words_;
};
#endif
