#ifndef __CWC_DECODER_H__
#define __CWC_DECODER_H__

#include "btd_2d.h"
#include "btem_cc_decoder.h"
#include "em_cc_decoder.h"
#include "em_wc_decoder.h"
#include "phonetic_index.h"
#include <memory>
#include <set>
#include <string>

class CwcDecoder : public EMWcDecoder {
public:
    class TouchInputWord : public InputWord {
    public:
        TouchInputWord(CwcDecoder& parent)
            : m_val(0)
            , m_parent(parent)
        {
        }
        TouchInputWord(const std::vector<Touch::Point<float>>& val, CwcDecoder& parent)
            : m_val(val)
            , m_parent(parent)
        {
        }
        TouchInputWord(const TouchInputWord& other)
            : m_val(other.m_val)
            , m_parent(other.m_parent)
        {
        }
        virtual ~TouchInputWord() {}
        TouchInputWord& operator=(const TouchInputWord& other)
        {
            m_val = other.m_val;
            return *this;
        }
        TouchInputWord& operator=(const std::vector<Touch::Point<float>>& val)
        {
            m_val = val;
            return *this;
        }
        operator std::vector<Touch::Point<float>>() { return m_val; }
        Touch::Point<float> operator[](int i) { return m_val[i]; }
        virtual int getClassId() const { return TOUCH_INPUT_WORD; }
        virtual std::string toString() const
        {
            std::string ret_str;
            for (auto itr = m_val.begin(); itr != m_val.end(); ++itr) {
                ret_str += m_parent.m_keyboard_.getShortestKey(*itr);
            }
            return ret_str;
        }

    protected:
        std::vector<Touch::Point<float>> m_val;
        CwcDecoder& m_parent;
    };

    CwcDecoder(EMCcDecoder& cc_decoder, std::string lm_name, std::string em_name, Keyboard& keyboard, int em_order = 4, float cer = 0.02, float lambda = 1.2, bool is_verbose = true)
        : EMWcDecoder(lm_name, em_name, em_order, cer, lambda, is_verbose)
        , m_cc_decoder_(cc_decoder)
        , m_keyboard_(keyboard)
    {
        mp_phonetic_index_ = new PhoneticIndex(PhoneticIndex::buildPIPath(), false);
        mp_btd2d_ = std::make_shared<Btd2D>(m_keyboard_.getDiameter(), Btd2D::NORMAL);
        m_keyboard_.setBtd2D(mp_btd2d_);
    }
    virtual ~CwcDecoder()
    {
        delete mp_phonetic_index_;
    }
    void setContext(std::string context);
    AbstractDecoder::DString decodeKey(InputKey& v, int beam_width = 10);
    std::string backKey();
    std::vector<std::pair<DString, float>> decodeWord(int k = 5);
    std::vector<std::pair<std::string, float>> predictChar(const int vocaSize = 1024);
    std::vector<std::pair<std::string, float>> predictWord(const int vocaSize = 1024, std::string c = "");
    char getChar(int x, int y)
    {
        return m_keyboard_.getShortestKey(Point<int>(x, y));
    }

protected:
    virtual std::set<DString> generateCandidates(InputKey& v_word);
    virtual float calcLogLikeliEM(std::string context, InputKey& v_word, DString w_word);
    virtual float calcLogLikeliBTEM(std::string context, TouchInputWord& v_word, DString w_word);
    EMCcDecoder& m_cc_decoder_;
    std::string m_context_;
    std::string m_context_ori_;
    DString m_v_word_;
    std::vector<std::vector<std::pair<DString, float>>> m_w_candidates_stack_;
    std::vector<std::pair<DString, float>> m_w_candidates_;

private:
    PhoneticIndex* mp_phonetic_index_;
    Touch::Keyboard& m_keyboard_;
    std::shared_ptr<Touch::Btd2D> mp_btd2d_;
};
#endif
