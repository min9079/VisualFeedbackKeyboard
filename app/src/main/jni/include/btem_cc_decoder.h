#ifndef __BTEM_CC_DECODER_H__
#define __BTEM_CC_DECODER_H__

#include "btd_2d.h"
#include "em_cc_decoder.h"
#include <memory>
#include <set>
#include <string>

using namespace Touch;

class BTEMCcDecoder : public EMCcDecoder {
public:
    class TouchInputKey : public CharInputKey {
    public:
        TouchInputKey(const Point<float>& val, BTEMCcDecoder& parent)
            : m_val(val)
            , m_parent(parent)
        {
        }
        TouchInputKey(const TouchInputKey& other)
            : m_val(other.m_val)
            , m_parent(other.m_parent)
        {
        }
        virtual ~TouchInputKey() {}
        TouchInputKey& operator=(const TouchInputKey& other)
        {
            m_val = other.m_val;
            return *this;
        }
        TouchInputKey& operator=(const Point<float>& val)
        {
            m_val = val;
            return *this;
        }
        operator Point<float>() { return m_val; }
        Point<float> getPoint() { return m_val; }
        virtual operator char()
        {
            return m_parent.m_keyboard_.getShortestKey(m_val);
        }
        virtual int getClassId() const { return TOUCH_INPUT_KEY; }
        virtual std::string toString() const
        {
            char char_buf[1024];
            sprintf(char_buf, "(%f,%f)", m_val.x, m_val.y);
            std::string ret(char_buf);
            return ret;
        }

    protected:
        Point<float> m_val;
        BTEMCcDecoder& m_parent;
    };
    BTEMCcDecoder(std::string lm_name, std::string em_name, Keyboard& keyboard, int em_order = 4, float cer = 0.02, float lambda = 1.2, Btd2D::FingerType finger = Btd2D::NORMAL, bool is_verbose = true);
    virtual ~BTEMCcDecoder();

    virtual std::pair<std::string, std::string> parseInputSentence(std::string sentence);

protected:
    virtual std::set<DString> generateCandidates(InputKey& v);
    virtual float calcLogLikeliTypingEM(InputKey& v, DString w);
    virtual float calcLogLikeli(std::string context, InputKey& v, DString w);

    Keyboard& m_keyboard_;
    std::shared_ptr<Btd2D> mp_btd2d_;

private:
};
#endif
