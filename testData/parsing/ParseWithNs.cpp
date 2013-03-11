namespace Mode
{
  class Mode_error: public Msg
  {
  public:
    typedef Mode_error const * R;
    Str::C const msg;
    Mode_error (                      Str::C const _msg, Text::Seg const text);
    Mode_error (Msg::Kind const kind, Str::C const _msg, Text::Seg const text);
    virtual void out (Str::Buf<> & out, Text::Seg const text) const;
  };

  class Error;
  class Unknown;
  class Based;
# define DefHash(Type,Init) class Type;
# include "mode.tab.inc"
# undef DefHash

  template <class M>
  M const * hash__clone (Allocate::T & store, M const * const src)
  { return POOL_NEW (store, M) (*src); }

  extern Function const * hash__clone (Allocate::T & store, Function const * const src);

# include "mode.tab.h"

  class FunctionRequired: public Msg
  {
  public:
    FunctionRequired (Text::Seg const _text);
    virtual void out (Str::Buf<> & out, Text::Seg const text) const;
  };
}

// comment \
continues